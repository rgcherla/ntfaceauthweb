package servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import awsrekognition.AWSRekognitionUtil;
import transferobject.RekognitionResult;

/**
 * Servlet implementation class AWSRekognitionProcessingServlet
 */
@WebServlet(value="/processRekognition")
public class AWSRekognitionProcessingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AWSRekognitionProcessingServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		ServletContext applicationContext = getServletContext();
		String bucket = applicationContext.getInitParameter("bucket");
		String album = applicationContext.getInitParameter("album");
		String collectionId = applicationContext.getInitParameter("collectionId");
		String matchThreshold = applicationContext.getInitParameter("matchThreshold");
		Float matchThresholdFloat = Float.parseFloat(matchThreshold);
		
		System.out.println("Pre-configured S3 bucket : " +  bucket); 
		System.out.println("Pre-configured Face Collection : " +  collectionId); 
		System.out.println("Pre-configured Face Album : " +  album); 
		System.out.println("Pre-configured face matches threshold : " +  matchThreshold); 

		AWSRekognitionUtil awsRekognitionUtil = new AWSRekognitionUtil(bucket, collectionId);
		
		String sourceImageName = request.getParameter("sourceImage");
		
		sourceImageName = sourceImageName.substring(album.length()+1);		
		
		System.out.println("Source Image Name for comparision : " + sourceImageName);
		
		awsRekognitionUtil.storeFace(sourceImageName);
		
		List<RekognitionResult> rekognitionResults = awsRekognitionUtil.searchFaceAgainstImage(sourceImageName, matchThresholdFloat, 2);
		
		request.setAttribute("faceMatches", rekognitionResults);
		
		request.getRequestDispatcher("/result.jsp").forward(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
