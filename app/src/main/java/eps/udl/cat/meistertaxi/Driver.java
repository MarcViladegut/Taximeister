package eps.udl.cat.meistertaxi;

import java.util.HashMap;
import java.util.Map;

public class Driver extends User {
    private long licence;

    public Driver(){ }

    public Driver(User user, long licence) {
        super(user.getName(), user.getEmail(), user.isDriver());
        this.licence = licence;
    }

    public long getLicence() {
        return licence;
    }

    public void setLicence(long licence) {
        this.licence = licence;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", this.getName());
        result.put("surname", this.getSurname());
        result.put("email", this.getEmail());
        result.put("smoke", this.isSmoke());
        result.put("driver", this.isDriver());
        result.put("licence", this.getLicence());

        return result;
    }
}
