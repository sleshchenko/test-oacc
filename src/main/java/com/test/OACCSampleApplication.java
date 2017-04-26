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

public class OACCSampleApplication {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver").newInstance();

        // get a connection to the oacc database
        String url = "jdbc:postgresql://localhost:5555/oaccdb?user=oaccuser&password=oaccpwd";

        try (Connection con = DriverManager.getConnection(url)) {
            // get the access control context
            AccessControlContext accessControlContext = SQLAccessControlContextFactory.getAccessControlContext(con,
                                                                                                               "OACC",
                                                                                                               SQLProfile.PostgreSQL_9_3_RECURSIVE);

            // create new admin
            createAdmin(accessControlContext);

            // create new user
            createUser(accessControlContext);

            // login as admin
            loginAdmin(accessControlContext, "adminJoe", "pa55w0rd");

            // attempt to update user while logged in as admin
            updateUser(accessControlContext, "jsmith");
        }
    }

    private static void createAdmin(AccessControlContext accessControlContext) {
        // authenticate as the system resource (the super user) to set up an initial admin
        accessControlContext.authenticate(Resources.getInstance(0),
                                          PasswordCredentials.newInstance("yourOaccSystemPassword".toCharArray()));

//         persist the admin in your application
//         for example:
        User admin = new User.Builder().login("adminJoe")
                                       .email("joeBloe@company.com")
                                       .admin(true)
                                       .build()
                                       .create();

        // create the corresponding OACC resource
        final Resource adminResource = accessControlContext.createResource("ADMIN",
                                                                           "APP_DOMAIN",
                                                                           admin.getLogin(),
                                                                           PasswordCredentials.newInstance("pa55w0rd".toCharArray()));

        System.out.println("created new ADMIN resource with Id=" + adminResource.getId());

        // grant permissions to query about, view and deactivate any user account, but not to edit it
        Set<ResourcePermission> permissions = new HashSet<>();
        permissions.add(ResourcePermissions.getInstance(ResourcePermissions.QUERY));
        permissions.add(ResourcePermissions.getInstance("VIEW"));
        permissions.add(ResourcePermissions.getInstance("DEACTIVATE"));

        accessControlContext.setGlobalResourcePermissions(adminResource,
                                                          "USER",
                                                          "APP_DOMAIN",
                                                          permissions);
        accessControlContext.unauthenticate();
    }

    private static void createUser(AccessControlContext accessControlContext) {
        // persist the user in your application
        // e.g. UserHOME.create("jsmith", "Jane", "Smith", "jsmith@mail.com", userResource.getId())
        // for example:
        User user = new User.Builder().login("jsmith")
                                      .email("jsmith@mail.com")
                                      .admin(false)
                                      .build()
                                      .create();

        // don't have to be authenticated to create users because
        // the resource class has the unauthenticatedCreateAllowed-flag set

        final Resource userResource = accessControlContext.createResource("USER",
                                                                          "APP_DOMAIN",
                                                                          user.getLogin(),
                                                                          PasswordCredentials.newInstance("pa$$word1".toCharArray()));
        System.out.println("created new USER resource with Id=" + userResource.getId());
    }

    private static void loginAdmin(AccessControlContext accessControlContext,
                                   String adminLogin,
                                   String password) {
        // authenticate as the admin resource
        accessControlContext.authenticate(Resources.getInstance(adminLogin),
                                          PasswordCredentials.newInstance(password.toCharArray()));
    }

    private static void updateUser(AccessControlContext accessControlContext,
                                   String userLogin) {
        // assert that the authenticated admin has VIEW permission *before* attempting to load the user
        accessControlContext.assertResourcePermissions(accessControlContext.getSessionResource(),
                                                       Resources.getInstance(userLogin),
                                                       ResourcePermissions.getInstance("VIEW"));

        // load the user information and modify the local copy
        User user = new User.Finder().findByLogin(userLogin);
        user.setEmail("other@mail.com");

        // assert that the authenticated admin has EDIT permission *before* attempting to save the user
        accessControlContext.assertResourcePermissions(accessControlContext.getSessionResource(),
                                                       Resources.getInstance(userLogin),
                                                       ResourcePermissions.getInstance("EDIT"));

        // save the user
        // !NOTE! we won't get here because adminResource doesn't have EDIT permission on the userResource
        user.save();
    }
}
