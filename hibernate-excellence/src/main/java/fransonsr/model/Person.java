package fransonsr.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@NamedQueries({
       @NamedQuery(name = "personDeleteById",
                   query = "delete from Person where id = :id")
})
public class Person {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Address address;

    @Id
    @GeneratedValue
    @Column(name = "person_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    @Length(max = 100)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @NotNull
    @Length(max = 100)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @NotNull
    @Length(max = 100)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @OneToOne(cascade = { CascadeType.ALL })
    @JoinTable(
           name = "person_address",
           joinColumns = {@JoinColumn(name = "person_id")},
           inverseJoinColumns = {@JoinColumn(name = "address_id")}
    )
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
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
        Person other = (Person) obj;
        if (firstName == null) {
            if (other.getFirstName() != null)
                return false;
        }
        else if (!firstName.equals(other.getFirstName()))
            return false;
        if (lastName == null) {
            if (other.getLastName() != null)
                return false;
        }
        else if (!lastName.equals(other.getLastName()))
            return false;
        return true;
    }

}
