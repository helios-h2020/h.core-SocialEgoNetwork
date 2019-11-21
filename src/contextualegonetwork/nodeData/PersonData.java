package contextualegonetwork.nodeData;

import java.util.Date;

public class PersonData extends NodeData {

    /**
     * First name of the user
     */
    private String firstName;
    /**
     * Second name of the user
     */
    private String secondName;
    /**
     * Surname of the user
     */
    private String surname;
    /**
     * Birth date of the person
     */
    private String birthDate;

    /**
     * Constructor method
     * @param identifier Unique identifier of the node
     * @param username Username of the node
     * @param firstName First name of the person
     * @param secondName Second name of the person
     * @param surname Surname of the person
     * @param birthDate Birth date of the user
     * @throws NullPointerException if username, firstName, secondName or surName are null
     * @throws IllegalArgumentException if userName, firstName or surname are empty strings
     */
    public PersonData(String firstName, String secondName, String surname, String birthDate) {
    	super("Person");
        if(firstName == null || secondName == null || surname == null) throw new NullPointerException();
        if(firstName.equals("") || surname.equals("")) throw new IllegalArgumentException("First name, surname or username cannot be empty strings");
        this.firstName = firstName;
        this.secondName = secondName;
        this.surname = surname;
        this.birthDate = birthDate;
    }
    
    /**
     * Needed for deserialization.
     */
    protected PersonData() {
    }
    
    /**
     *
     * @return The first name of the user
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     *
     * @return The second name of the user
     */
    public String getSecondName() {
        return this.secondName;
    }

    /**
     *
     * @return The surname of the user
     */
    public String getSurname() {
        return this.surname;
    }
    
    /**
     * @return The birthdate of the user
     */
    public Date getBirthday() {
    	return new Date(birthDate);
    }
}
