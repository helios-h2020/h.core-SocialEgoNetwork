package contextualegonetwork;

import com.fasterxml.jackson.annotation.JsonCreator;

class Person extends Node {

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
     * Birth date of the person (expressed as a timestamp)
     */
    private long birthDate;

    /**
     * Used in deserialization
     */
    @JsonCreator
    public Person()
    {}
    /**
     * Constructor method
     * @param username Username of the node
     * @param identifier Numeric identifier of the node
     * @param firstName First name of the person
     * @param secondName Second name of the person
     * @param surname Surname of the person
     * @param birthDate Birth date of the user
     * @throws NullPointerException if username, firstName, secondName or surName are null
     * @throws IllegalArgumentException if userName, firstName or surname are empty strings
     */
    public Person(String username, int identifier, String firstName, String secondName, String surname, long birthDate) {
        super(username, identifier);
        if(firstName == null || secondName == null || surname == null) throw new NullPointerException();
        if(firstName.equals("") || surname.equals("") || username.equals("")) throw new IllegalArgumentException("First name, surname or username cannot be empty strings");
        this.firstName = firstName;
        this.secondName = secondName;
        this.surname = surname;
        this.birthDate = birthDate;
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
}
