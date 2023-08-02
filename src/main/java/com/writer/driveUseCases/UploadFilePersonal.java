package com.writer.driveUseCases;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;

public class UploadFilePersonal {
    private static final String APPLICATION_NAME = "My Uploads";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        EventQueue.invokeLater(() -> {
            JFrame frame = new UploadAppFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    static Credential authorize(HttpTransport httpTransport, Properties properties) throws IOException {
        String clientSecretsPath = properties.getProperty("client_secrets_path");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(clientSecretsPath)));

        String tokensDirectoryPath = System.getProperty("user.home") + File.separator + ".credentials" + File.separator + properties.getProperty("tokens_directory_path");

        List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/drive");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    static void uploadFilesFromFolder(HttpTransport httpTransport, Credential credential, String folderPath) throws IOException {
        Drive driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isFile()) {
                    uploadFile(driveService, file);
                }
            }
        }
    }

    private static void uploadFile(Drive driveService, java.io.File localFile) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(localFile.getName());

        // Set any additional properties for the file, such as folder location, etc.

        FileContent mediaContent = new FileContent("", localFile);
        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        System.out.println("File uploaded successfully. File ID: " + uploadedFile.getId());
    }

    static Properties loadConfigProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        }
        return properties;
    }
}

class UploadAppFrame extends JFrame {
    private JButton uploadButton;

    public UploadAppFrame() {
        setTitle("Google Drive File Upload");
        setSize(300, 200);
        setLocationRelativeTo(null);

        uploadButton = new JButton("Upload File");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performFileUpload();
            }
        });

        JPanel panel = new JPanel();
        panel.add(uploadButton);
        add(panel);
    }

    private void performFileUpload() {
        try {
            Properties properties = UploadFilePersonal.loadConfigProperties();
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = UploadFilePersonal.authorize(httpTransport, properties);
            String folderPath = properties.getProperty("folder_path");

            UploadFilePersonal.uploadFilesFromFolder(httpTransport, credential, folderPath);

            // Show success message
            JOptionPane.showMessageDialog(this, "File(s) uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | GeneralSecurityException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred during file upload: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
