import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageHandler implements HttpHandler {
    public ImageHandler(){
        Props.loadProperties();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equalsIgnoreCase("POST") && exchange.getRequestURI().getPath().equals(Props.getValueOf("endpoint"))){

            // Retrieve the filename from the Content-Disposition header
            Headers headers = exchange.getRequestHeaders();
            String fileName = this.getFileNameFromHeaders(headers);
            String imageName = fileName;

            // Setting the input and output directory name
            if(! fileName.equals("default"))
                fileName = fileName.substring(0, fileName.indexOf('.'));
            String inputDirectoryPath = "/home/skinan/project/docker_input/" + fileName + "_input/" + fileName + "/pet";
            String outputDirectoryPath = "/home/skinan/project/docker_output/" + fileName + "_output/" + fileName;
            String resultPathForResponse = outputDirectoryPath + "/predicted_data/predicted_pseudo_3d_reconstructed/" + fileName + "/" + fileName + "_3d_reconstructed_predicted.nii" ;
            Path imagePath = Paths.get(inputDirectoryPath, imageName);
            try{
                Files.createDirectories(Path.of(inputDirectoryPath));
                Files.createDirectories(Path.of(outputDirectoryPath));
            }catch(Exception e){
                System.out.println(e.getMessage());
            }

            System.out.println("File '" + fileName + "' has been received.");	

            // Handling the request.
            this.saveImage(exchange.getRequestBody(), imagePath);

            // Executing the docker script.
            this.executeDockerScript(fileName);

            // Handling the response.
            this.sendResponse(exchange, Paths.get(resultPathForResponse));

            // Removing the input and output files from the server.
            this.deleteFolder(new File("/home/skinan/project/docker_output"));
            this.deleteFolder(new File("/home/skinan/project/docker_input"));
        }
        else{
            exchange.sendResponseHeaders(405, -1);
        }
    }

    public String getFileNameFromHeaders(Headers headers){
        if (headers.containsKey("Content-Disposition")) {
            String contentDisposition = headers.getFirst("Content-Disposition");
            String[] tokens = contentDisposition.split(";");
            for (String token : tokens) {
                if (token.trim().startsWith("filename"))
                    return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "default";
    }
    public void saveImage(InputStream inputStream, Path imagePath){
        try(OutputStream outputStream = new FileOutputStream(imagePath.toFile())){
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, bytesRead);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public void executeDockerScript(String input){
        final String scriptPath = Props.getValueOf("dockerScriptPath");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", scriptPath, input);

        try{
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while((line = reader.readLine()) != null)
                System.out.println(line);

            int exitCode = process.waitFor();
            System.out.println("\nScript executed with exit code: " + exitCode);

        }catch(IOException | InterruptedException e){
            System.out.println(e.getMessage());
        }
    }
    
    public void sendResponse(HttpExchange exchange, Path imagePath) {
        try {
            // Read the image file into a byte array
            byte[] imageBytes = Files.readAllBytes(imagePath);
            // Set the proper headers
            exchange.getResponseHeaders().set("Content-Type", "image/*"); // Adjust content type if needed
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(imageBytes.length));
            // Send the response headers
            exchange.sendResponseHeaders(200, imageBytes.length);
            // Write the image bytes to the response body
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(imageBytes);
            outputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteFolder(File folder){
        if(folder.isDirectory()){
            final File [] files = folder.listFiles();
            if(files != null)
                for(File file: files)
                    deleteFolder(file);
        }
        folder.delete();
    }
}
