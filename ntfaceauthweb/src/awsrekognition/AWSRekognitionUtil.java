package awsrekognition;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.CreateCollectionResult;
import com.amazonaws.services.rekognition.model.FaceMatch;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest;
import com.amazonaws.services.rekognition.model.SearchFacesByImageResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import transferobject.RekognitionResult;

public class AWSRekognitionUtil {
		
	private String collectionId;
	private String bucket;
	private AmazonRekognition amazonRekognition; 
	private AWSCredentials credentials;
	private AmazonS3 s3client;
	
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	/**
	 * Constructor to initialize the instance parameters.
	 * @param bucket
	 * @param collectionId
	 */
	public AWSRekognitionUtil(String bucket, String collectionId) {
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
	        s3client = new AmazonS3Client(credentials);

			amazonRekognition = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_WEST_2)
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

	        setBucket(bucket);
	        setCollectionId(collectionId);

		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/userid/.aws/credentials), and is in valid format.", e);
		}
	}
	
	/**
	 * Operation for uploading a source image to the S3, since image has to be made available in S3 to index 
	 * it and extract facial metadata, before comparison with a target.
	 * @param keyName
	 * @param uploadFileName
	 */
	public void uploadToAmazonS3(String keyName, String uploadFileName) {
        
		URI fileURI = null;
		try {
        	System.out.println("Uploading a new object to S3 from a file\n");

        	ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        	fileURI = classloader.getResource(uploadFileName).toURI();
        	
        	System.out.println("File URI: " + fileURI);
        	
            File file = new File(fileURI);
            s3client.putObject(new PutObjectRequest(
            		                 bucket, keyName, file));

            System.out.println("Upload successful for " + uploadFileName);
            
         } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
            		"means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
            		"means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (URISyntaxException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Collection to be created is set as part of the constructor of class.
	 */
	public void createFaceCollection() {

		System.out.println("Creating collection: " + collectionId);

		CreateCollectionRequest request = new CreateCollectionRequest().withCollectionId(collectionId);

		CreateCollectionResult createCollectionResult = amazonRekognition.createCollection(request);
		System.out.println("CollectionArn : " + createCollectionResult.getCollectionArn());
		System.out.println("Status code : " + createCollectionResult.getStatusCode().toString());
	}

	/**
	 * Source Image Name is the customer's image which is already uploaded and available in S3.
	 * The operation detects face and add it to the collection specified. Facial features are extracted 
	 * and stored to database. Face metadata is also stored.
	 * @param sourceImageName
	 */
	public void storeFace(String sourceImageName) {
		
			Image source = new Image().withS3Object(new S3Object().withBucket(bucket).withName(sourceImageName));

			IndexFacesRequest indexFacesRequest = new IndexFacesRequest().withImage(source).withCollectionId(collectionId)
					.withExternalImageId(sourceImageName).withDetectionAttributes("ALL");

			IndexFacesResult indexFacesResult = amazonRekognition.indexFaces(indexFacesRequest);

			System.out.println(sourceImageName + " added");
			List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
			for (FaceRecord faceRecord : faceRecords) {
				System.out.println("Face detected: Faceid is " + faceRecord.getFace().getFaceId());
			}
	}
	
	/**
	 * Search the image supplied against a set of faces in the collection, for similarity.
	 * @param imageForSearch
	 */
	public List<RekognitionResult> searchFaceAgainstImage(String imageForSearch, Float matchThreshold, int maxFacesToReturn) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<RekognitionResult> rekognitionResults = new ArrayList<RekognitionResult>();
		RekognitionResult rekognitionResult = null;
		
		// Get an image object from S3 bucket for the image input.
		Image image = new Image().withS3Object(new S3Object().withBucket(bucket).withName(imageForSearch));
				
		// Search collection for faces similar to the largest face in the image.
		SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
				.withCollectionId(collectionId).withImage(image).withFaceMatchThreshold(matchThreshold).withMaxFaces(maxFacesToReturn);

		SearchFacesByImageResult searchFacesByImageResult = amazonRekognition
				.searchFacesByImage(searchFacesByImageRequest);
		System.out.println("Faces matching largest face in image from " + imageForSearch);
		List<FaceMatch> faceImageMatches = searchFacesByImageResult.getFaceMatches();
		for (FaceMatch face : faceImageMatches) {
			try {
				rekognitionResult = new RekognitionResult();
				rekognitionResult.setMatchedFace(face.getFace());
				rekognitionResult.setConfidence(face.getSimilarity());
				rekognitionResult.setCustomerImageName(imageForSearch);
				
				rekognitionResults.add(rekognitionResult);
				System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(face));
			} catch (JsonProcessingException e) {
				System.out.println(face.getFace().getFaceId());
				System.out.println(e.getMessage());
			}
		}
		return rekognitionResults;
	}
	
}
