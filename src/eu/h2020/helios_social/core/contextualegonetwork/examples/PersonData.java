package eu.h2020.helios_social.core.contextualegonetwork.examples;

public class PersonData {

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
     * @param firstName First name of the person
     * @param secondName Second name of the person
     * @param surname Surname of the person
     * @param birthDate Birth date of the user
     * @throws NullPointerException if username, firstName, secondName or surName are null
     * @throws IllegalArgumentException if userName, firstName or surname are empty strings
     */
    public PersonData(String firstName, String secondName, String surname, String birthDate) {
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
        return firstName;
    }

    /**
     *
     * @return The second name of the user
     */
    public String getSecondName() {
        return secondName;
    }

    /**
     *
     * @return The surname of the user
     */
    public String getSurname() {
        return surname;
    }
    
    /**
     * @return The birthdate of the user
     */
    public String getBirthday() {
    	return birthDate;
    }
    
    @Override
    public String toString() {
    	return firstName+" "+secondName+" "+surname+", born "+birthDate;
    }
}
