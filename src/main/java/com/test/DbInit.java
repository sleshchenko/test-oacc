package com.test;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Sergii Leschenko
 */
public class DbInit {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver").newInstance();

        // get a connection to the oacc database
        String url = "jdbc:postgresql://localhost:5555/oaccdb?user=oaccuser&password=oaccpwd";

        try (Connection con = DriverManager.getConnection(url)) {
            com.acciente.oacc.sql.internal.SQLAccessControlSystemInitializer.initializeOACC(con,
                                                                                            "oacc",
                                                                                            "test".toCharArray());
        }
    }
}
