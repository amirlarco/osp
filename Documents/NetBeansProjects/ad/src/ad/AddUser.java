/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad;

/**
 *
 * @author HP
 */
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class AddUser {

    private static final String DOMAIN_NAME = "sdc";
    private static final String DOMAIN_ROOT = "DC=sdc,DC=local";
    private static final String DOMAIN_URL = "ldap://10.0.6.84:389";
    private static final String ADMIN_NAME = "CN=Mohammad Amir,CN=Users,DC=sdc,DC=local";
    private static final String ADMIN_PASS = "Amber#lari";

    private String userName, firstName, lastName, password, organisationUnit;
    private LdapContext context;

    public AddUser(String userName, String firstName, String lastName,String password, String organisationUnit) {

        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.organisationUnit = organisationUnit;

        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ADMIN_NAME);
        env.put(Context.SECURITY_CREDENTIALS, ADMIN_PASS);

        // connect to my domain controller
        env.put(Context.PROVIDER_URL, DOMAIN_URL);

        try {
            this.context = new InitialLdapContext(env, null);
        } catch (NamingException e) {
            System.err.println("Problem creating object: ");
            e.printStackTrace();
        }

    }

    public boolean addUser() throws NamingException {

        // Create a container set of attributes
        Attributes container = new BasicAttributes();

        // Create the objectclass to add
        Attribute objClasses = new BasicAttribute("objectClass");
        objClasses.add("top");
        objClasses.add("person");
        objClasses.add("organizationalPerson");
        objClasses.add("user");

        // Assign the username, first name, and last name
        String cnValue = new StringBuffer(firstName).append(" ").append(lastName).toString();
        Attribute cn = new BasicAttribute("cn", cnValue);
        Attribute sAMAccountName = new BasicAttribute("sAMAccountName", userName);
        Attribute principalName = new BasicAttribute("userPrincipalName", userName + "@" + DOMAIN_NAME);
        Attribute givenName = new BasicAttribute("givenName", firstName);
        Attribute sn = new BasicAttribute("sn", lastName);
        Attribute uid = new BasicAttribute("uid", userName);

        // Add password
        Attribute userPassword = new BasicAttribute("userpassword", password);

        // Add these to the container
        container.put(objClasses);
        container.put(sAMAccountName);
        container.put(principalName);
        container.put(cn);
        container.put(sn);
        container.put(givenName);
        container.put(uid);
        container.put(userPassword);

        // Create the entry
        try {
            context.createSubcontext(getUserDN(cnValue, organisationUnit), container);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getUserDN(String aUsername, String aOU) {
        return "cn=" + aUsername + ",ou=" + aOU + "," + DOMAIN_ROOT;
    }
}
