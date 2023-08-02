package com.writer.driveUseCases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;

public class DownloadFilePersonal {
	  private static final String APPLICATION_NAME = "Writer Downloads";
	    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + ".credentials" + File.separator + "my-application-name";


	    public static void main(String[] args) throws IOException, GeneralSecurityException {
	        // Build the HTTP transport and set up the credentials
	        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	        Credential credential = authorize(httpTransport);


	        downloadFile(httpTransport, credential, "1o_dsC2d1vc_AZqHPp-1scDiK4SSkaYK9", "C:\\Users\\Shamal Shinde\\Downloads\\abc.pdf");
	    }

	    private static Credential authorize(HttpTransport httpTransport) throws IOException {
	      

	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
	        	    new InputStreamReader(new FileInputStream("C:/Users/Shamal Shinde/Downloads/client_secrets123.json")));

	        
	        List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/drive.readonly");

	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	                httpTransport, JSON_FACTORY, clientSecrets, scopes)
	                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
	                .setAccessType("offline")
	                .build();

	        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
	        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	    }

	    private static void downloadFile(HttpTransport httpTransport, Credential credential, String fileId, String destinationFileName) throws IOException {
	        Drive driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
	                .setApplicationName(APPLICATION_NAME)
	                .build();

	        // Get the file metadata
	        com.google.api.services.drive.model.File fileMetadata = driveService.files().get(fileId).execute();
	        
	        // Download the file content
	        OutputStream outputStream = new FileOutputStream(destinationFileName);
	        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
	        outputStream.close();

	        System.out.println("File downloaded successfully: " + destinationFileName);
	    }

}
