package Security;

import Client.FolderStruct;

public class Authentication {
    private static Authentication single_instance = null;
    private String passwordInsert;

    public void setPasswordInsert(String passwordInsert){this.passwordInsert = passwordInsert;}

    public String getPasswordInsert(){return passwordInsert;}

    public static Authentication getInstance() {
        if (single_instance == null)
            single_instance = new Authentication();

        return single_instance;
    }
}
