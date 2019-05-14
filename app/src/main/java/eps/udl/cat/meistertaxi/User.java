package eps.udl.cat.meistertaxi;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String name;
    private String surname;
    private String email;
    private int gender;
    private boolean smoke;
    private boolean driver;

    public User(String name, String email, boolean driver) {
        this.name = name;
        this.surname = "";
        this.email = email;
        this.gender = 0; // 0: None 1: Women 2: Men
        this.smoke = false;
        this.driver = driver;
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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
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

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("surname", surname);
        result.put("email", email);
        result.put("gender", gender);
        result.put("smoke", smoke);
        result.put("driver", driver);

        return result;
    }
}
