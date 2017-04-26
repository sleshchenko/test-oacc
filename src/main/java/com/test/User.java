package com.test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergii Leschenko
 */
public class User {
    private static Map<String, User> data = new HashMap<>();

    private String  login;
    private String  email;
    private boolean isAdmin;

    private User(String login, String email, boolean isAdmin) {
        this.login = login;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public User(User user) {
        this(user.login, user.email, user.isAdmin);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public User create() {
        return data.put(this.login, this);
    }

    public void save() {
        data.put(this.login, this);
    }

    public static class Builder {
        private String  email;
        private String  login;
        private boolean isAdmin;

        public Builder login(String login) {
            this.login = login;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public User build() {
            return new User(login, email, isAdmin);
        }

        public Builder admin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }
    }

    public static class Finder {
        public User findByLogin(String login) {
            return new User(data.get(login));
        }
    }
}
