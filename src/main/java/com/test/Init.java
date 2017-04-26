package com.test;

import com.acciente.oacc.AccessControlContext;
import com.acciente.oacc.PasswordCredentials;
import com.acciente.oacc.Resource;
import com.acciente.oacc.ResourcePermission;
import com.acciente.oacc.ResourcePermissions;
import com.acciente.oacc.Resources;
import com.acciente.oacc.sql.SQLAccessControlContextFactory;
import com.acciente.oacc.sql.SQLProfile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Set;

public class Init {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver").newInstance();

        // get a connection to the oacc database
        String url = "jdbc:postgresql://localhost:5555/oaccdb?user=oaccuser&password=oaccpwd";

        try (Connection con = DriverManager.getConnection(url)) {
            // get the access control context
            AccessControlContext accessControlContext = SQLAccessControlContextFactory.getAccessControlContext(con,
                                                                                                               "OACC",
                                                                                                               SQLProfile.PostgreSQL_9_3_RECURSIVE);

            accessControlContext.authenticate(Resources.getInstance(0),
                                              PasswordCredentials.newInstance("test".toCharArray()));

            // authenticate as the system resource
            accessControlContext.createDomain("APP_DOMAIN");


            accessControlContext.createResourceClass("USER", true, true);
            accessControlContext.createResourceClass("ADMIN", true, false);
            accessControlContext.createResourceClass("DOCUMENT", false, false);

            // permissions on resources of class "USER"
            accessControlContext.createResourcePermission("USER", "VIEW");
            accessControlContext.createResourcePermission("USER", "EDIT");
            accessControlContext.createResourcePermission("USER", "DEACTIVATE");

            // permissions on resources of class "ADMIN"
            accessControlContext.createResourcePermission("ADMIN", "EDIT");
            accessControlContext.createResourcePermission("ADMIN", "DEACTIVATE");

            // permissions on resources of class "DOCUMENT"
            accessControlContext.createResourcePermission("DOCUMENT", "READ");
            accessControlContext.createResourcePermission("DOCUMENT", "UPDATE");
            accessControlContext.createResourcePermission("DOCUMENT", "COPY");
            accessControlContext.createResourcePermission("DOCUMENT", "PRINT");
        }
    }
}
