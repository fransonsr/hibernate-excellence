package fransonsr.model;

public class Address {

    private String street;
    private String city;
    private String state;
    private String zip;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((street == null) ? 0 : street.hashCode());
        result = prime * result + ((zip == null) ? 0 : zip.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (city == null) {
            if (other.city != null)
                return false;
        }
        else if (!city.equals(other.getCity()))
            return false;
        if (state == null) {
            if (other.getState() != null)
                return false;
        }
        else if (!state.equals(other.getState()))
            return false;
        if (street == null) {
            if (other.getStreet() != null)
                return false;
        }
        else if (!street.equals(other.getStreet()))
            return false;
        if (zip == null) {
            if (other.getZip() != null)
                return false;
        }
        else if (!zip.equals(other.getZip()))
            return false;
        return true;
    }


}
