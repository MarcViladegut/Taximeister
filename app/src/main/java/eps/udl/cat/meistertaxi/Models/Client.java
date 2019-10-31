package eps.udl.cat.meistertaxi.Models;

import java.util.HashMap;
import java.util.Map;

public class Client extends User {
    private int gender;

    public Client() { }

    public Client(User user, int gender) {
        super(user.getName(), user.getEmail(), user.isDriver(), user.getToken());
        this.gender = gender;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    /* Method to update information on database */
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", this.getName());
        result.put("surname", this.getSurname());
        result.put("email", this.getEmail());
        result.put("gender", gender);
        result.put("driver", this.isDriver());
        result.put("smoke", this.isSmoke());
        result.put("token", this.getToken());

        return result;
    }
}
