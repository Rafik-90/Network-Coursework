

public class Client {
    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine("localhost", 8080);
        try {
            commandLine.runProgram();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
