/**
 * 
 */       
       var albumBucketName = 'insurance-customers';
       var bucketRegion = 'us-west-2'; // Region 
       var IdentityPoolId = 'us-west-2:55076397-49b0-47a1-a40e-41a6408fc1a4';

//AWS global environment configured with cognito access credentials and region 
       AWS.config.update({
         region: bucketRegion,
         credentials: new AWS.CognitoIdentityCredentials({
           IdentityPoolId: IdentityPoolId
         })
       });

       var s3 = new AWS.S3({						// S3 initialization using AWS global variable			
         apiVersion: '2006-03-01',					// CORS Config for this version of AWS
         params: {Bucket: albumBucketName}
       });

       // list available albums in bucket
       function listAlbums() {
         s3.listObjects({Delimiter: '/'}, function(err, data) {			
           if (err) {
             return alert('There was an error listing your albums: ' + err.message);
           } else {
             var albums = data.CommonPrefixes.map(function(commonPrefix) {
               var prefix = commonPrefix.Prefix;
               var albumName = decodeURIComponent(prefix.replace('/', ''));
               return getHtml([
                 '<li>',
                   '<span onclick="deleteAlbum(\'' + albumName + '\')">X</span>',
                   '<span onclick="viewAlbum(\'' + albumName + '\')">',
                     albumName,
                   '</span>',
                 '</li>'
               ]);
             });
             var message = albums.length ?
               getHtml([
                 '<p style="text-align:center"><b>Click on an album name to view it.</b></p>',
                 '<p>Click on the X to delete the album.</p>'
               ]) :
               '<p style="text-align:center">You do not have any albums. Please Create album.</p>';
             var htmlTemplate = [
               '<h2 style="color:red">Albums</h2>',
               message,
               '<ul>',
                 getHtml(albums),
               '</ul>',
               '<div style="text-align:center"><button onclick="createAlbum(prompt(\'Enter Album Name:\'))">',
                 'Create New Album',
               '</button></div>'
             ]
             document.getElementById('app').innerHTML = getHtml(htmlTemplate);
           }
         });
       }

       // creates a new album in bucket
       function createAlbum(albumName) {
         albumName = albumName.trim();
         if (!albumName) {
           return alert('Album names must contain at least one non-space character.');
         }
         if (albumName.indexOf('/') !== -1) {
           return alert('Album names cannot contain slashes.');
         }
         var albumKey = encodeURIComponent(albumName) + '/';
         s3.headObject({Key: albumKey}, function(err, data) {
           if (!err) {
             return alert('Album already exists.');
           }
           if (err.code !== 'NotFound') {
             return alert('There was an error creating your album: ' + err.message);
           }
           s3.putObject({Key: albumKey}, function(err, data) {
             if (err) {
               return alert('There was an error creating your album: ' + err.message);
             }
             alert('Successfully created album.');
             viewAlbum(albumName);
           });
         });
       }

       // view album
       function viewAlbum(albumName) {
         var albumPhotosKey = encodeURIComponent(albumName) + '/';
         s3.listObjects({Prefix: albumPhotosKey}, function(err, data) {
           if (err) {
             return alert('There was an error viewing your album: ' + err.message);
           }
           // `this` references the AWS.Response instance that represents the response
           var href = this.request.httpRequest.endpoint.href;
           var bucketUrl = href + albumBucketName + '/';

           var photos = data.Contents.map(function(photo) {
             var photoKey = photo.Key;
             var photoUrl = bucketUrl + encodeURIComponent(photoKey);
             return getHtml([
               '<span>',
                 '<div>',
                 '<img style="width:128px;height:128px;" src="' + photoUrl + '"/>',
                 '</div>',
                 '<div>',
                   '<span onclick="deletePhoto(\'' + albumName + "','" + photoKey + '\')">',
                     'X',
                   '</span>',
                   '<span>',
                     photoKey.replace(albumPhotosKey, ''),
                   '</span>',
                 '</div>',
               '<span>',
             ]);
           });
           var message = photos.length ?
             '<p>Click on the X to delete the photo</p>' :
             '<p>You do not have any photos in this album. Please add photos.</p>';
           var htmlTemplate = [
             '<h2 "color:red">',
               'Album: ' + albumName,
             '</h2>',
             message,
             '<div>',
               getHtml(photos),
             '</div>',
             '<div>',
             '<input id="photorekognition" type="file" accept="image/*">',
             '<button id="addPhotoRunRekognition" onclick="addPhotoRunRekognition(\'' + albumName + '\')">',
              'Capture Photo and Authenticate User',
              '</div>',
             '</button>',
             '<div>',
             '<input id="photoupload" type="file" accept="image/*">',
             '<button id="addphoto" onclick="addPhoto(\'' + albumName +'\')">',
               'Add Photo',
             '</div>',
             '<div>',
             '</button>',
             '<button onclick="listAlbums()">',
               'Back To Albums',
             '</button>',
             '</div>', 
             '<div>',
             '</div>',
           ]
           document.getElementById('app').innerHTML = getHtml(htmlTemplate);
         });
       }
       
       function runRekognition(albumName, photoKey) {
    	   document.location.href = "/ntfaceauthweb/processRekognition?sourceImage=" + photoKey;
       }

       // Add photo to an album and view 
       function addPhoto(albumName) {
         var files = document.getElementById('photoupload').files;
         if (!files.length) {
           return alert('Please choose a file to upload first.');
         }
         var file = files[0];
         var fileName = file.name;
         var albumPhotosKey = encodeURIComponent(albumName) + '/';

         var photoKey = albumPhotosKey + fileName;
         s3.upload({
           Key: photoKey,
           Body: file,
           ACL: 'public-read'
         }, function(err, data) {
           if (err) {
             return alert('There was an error uploading your photo: ', err.message);
           }
           alert('Successfully uploaded photo.');
           viewAlbum(albumName);
         });
       }

       //Add photo to album and run recognition
       function addPhotoRunRekognition(albumName) {
         var files = document.getElementById('photorekognition').files;
         if (!files.length) {
           return alert('Please choose a file to upload first.');
         }
         var file = files[0];
         var fileName = file.name;
         var albumPhotosKey = encodeURIComponent(albumName) + '/';

         var photoKey = albumPhotosKey + fileName;
         s3.upload({
           Key: photoKey,
           Body: file,
           ACL: 'public-read'
         }, function(err, data) {
           if (err) {
             return alert('There was an error uploading your photo: ', err.message);
           }
           alert('Successfully uploaded photo.');
           runRekognition(albumName, photoKey);
           alert('Successfully ran rekognition.');
           //viewAlbum(albumName);
         });
       }
       
       // Delete a photo from album
       function deletePhoto(albumName, photoKey) {
         s3.deleteObject({Key: photoKey}, function(err, data) {
           if (err) {
             return alert('There was an error deleting your photo: ', err.message);
           }
           alert('Successfully deleted photo.');
           viewAlbum(albumName);
         });
       }

       // Delete an album
       function deleteAlbum(albumName) {
         var albumKey = encodeURIComponent(albumName) + '/';
         s3.listObjects({Prefix: albumKey}, function(err, data) {
           if (err) {
             return alert('There was an error deleting your album: ', err.message);
           }
           var objects = data.Contents.map(function(object) {
             return {Key: object.Key};
           });
           s3.deleteObjects({
             Delete: {Objects: objects, Quiet: true}
           }, function(err, data) {
             if (err) {
               return alert('There was an error deleting your album: ', err.message);
             }
             alert('Successfully deleted album.');
             listAlbums();
           });
         });
       }