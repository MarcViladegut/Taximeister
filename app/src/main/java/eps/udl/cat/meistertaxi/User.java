package eps.udl.cat.meistertaxi;

public class User {

    private String name;
    private String surname;
    private String email;
    private boolean smoke;
    private boolean driver;
    private String token;

    public User(String name, String email) {
        this.name = name;
        this.surname = "";
        this.email = email;
        this.smoke = false;
    }

    public User(String name, String email, boolean driver) {
        this.name = name;
        this.surname = "";
        this.email = email;
        this.smoke = false;
        this.driver = driver;
        this.token = "";
    }

    public User(String name, String email, boolean driver, String token) {
        this.name = name;
        this.surname = "";
        this.email = email;
        this.smoke = false;
        this.driver = driver;
        this.token = token;
    }

    public User(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSmoke() {
        return smoke;
    }

    public void setSmoke(boolean smoke) {
        this.smoke = smoke;
    }

    public boolean isDriver() {
        return driver;
    }

    public void setDriver(boolean driver) {
        this.driver = driver;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
