/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

/**
 *
 * @author HP
 */
public class Ad {

    public static void main(String[] args) {

        Hashtable env = new Hashtable();
        String userName = "CN=Bokhari MU,OU=Professor,OU=Teaching,OU=Staff,OU=amu_users,DC=sdc,DC=local";
        String oldPassword = "Amber#lari";
        String newPassword = "Amir#lari";

        //Access the keystore, this is where the Root CA public key cert was installed
        //Could also do this via command line java -Djavax.net.ssl.trustStore....
        String keystore = "/usr/java/jdk1.5.0_01/jre/lib/security/cacerts";
        System.setProperty("javax.net.ssl.trustStore", keystore);

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        //set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userName);
        env.put(Context.SECURITY_CREDENTIALS, oldPassword);

        //specify use of ssl
        env.put(Context.SECURITY_PROTOCOL, "ssl");

        //connect to my domain controller
        String ldapURL = "ldaps://10.0.6.84:636";
        env.put(Context.PROVIDER_URL, ldapURL);

        try {

            // Create the initial directory context
            LdapContext ctx = new InitialLdapContext(env, null);

            //change password is a single ldap modify operation
            //that deletes the old password and adds the new password
            ModificationItem[] mods = new ModificationItem[2];

            //Firstly delete the "unicdodePwd" attribute, using the old password
            //Then add the new password,Passwords must be both Unicode and a quoted string
            String oldQuotedPassword = "\"" + oldPassword + "\"";
            byte[] oldUnicodePassword = oldQuotedPassword.getBytes("UTF-16LE");
            String newQuotedPassword = "\"" + newPassword + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("unicodePwd", oldUnicodePassword));
            mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));

            // Perform the update
            ctx.modifyAttributes(userName, mods);

            System.out.println("Changed Password for: " + userName);
            ctx.close();

        } catch (NamingException e) {
            System.err.println("Problem changing password: " + e);
        } catch (UnsupportedEncodingException e) {
            System.err.println("Problem encoding password: " + e);
        }

    }

//    public static void main(String[] args) {
//        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
//// the keystore that holds trusted root certificates 
//        System.setProperty("javax.net.ssl.trustStore", "c:\\myCaCerts.jks");
//        System.setProperty("javax.net.debug", "all");
//        ADConnection adc = new ADConnection();
//        adc.updatePassword("Java User2", "Amir#lari");
//    }
//    public static void main(String[] args) throws IOException, NamingException, LdapException {
//
//        LdapConnection con = new LdapNetworkConnection("10.0.6.84", 389);
//        con.bind("CN=Mohammad Amir,CN=Users,DC=sdc,DC=local", "Amber#lari");
//
//        if (con.isAuthenticated()) {
//            System.out.print("You are successfully authenticated with Acdtive Directory\n");
//        }
//        try {
//            con.add(new DefaultEntry(
//                    "CN=Mohammad Amir,OU=Professor,OU=Teaching,OU=Staff,OU=amu_users,DC=sdc,DC=local", // The Dn
//                    "ObjectClass: top",
//                    "ObjectClass: person",
//                    "cn: Mohammad Amir",
//                    "sn: Amir"
//            ));
//        } catch (Exception e) {
//            System.out.print("This is the error while adding entries :\n\n" +e.getMessage());            
//        }
//        Hashtable env = new Hashtable();
//        String adminName = "CN=Mohammad Amir,CN=Users,DC=sdc,DC=local";
//        String adminPassword = "Amber#lari";
//        String userName = "CN=Mohammad Amir,OU=Professor,OU=Teaching,OU=Staff,OU=amu_users,DC=sdc,DC=local";
//        String groupName = "OU=Professor,OU=Teaching,OU=Staff,OU=amu_users,DC=sdc,DC=local";
//
//        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//
//        //set security credentials, note using simple cleartext authentication
//        env.put(Context.SECURITY_AUTHENTICATION, "simple");
//        env.put(Context.SECURITY_PRINCIPAL, adminName);
//        env.put(Context.SECURITY_CREDENTIALS, adminPassword);
//
//        //connect to my domain controller
//        env.put(Context.PROVIDER_URL, "ldap://10.0.6.84:389");
//
//        try {
//
//            // Create the initial directory context
//            LdapContext ctx = new InitialLdapContext(env, null);
//
//            // Create attributes to be associated with the new user
//            Attributes attrs = new BasicAttributes(true);
//
//            //These are the mandatory attributes for a user object
//            //Note that Win2K3 will automagically create a random 
//            //samAccountName if it is not present. (Win2K does not)
//            attrs.put("objectClass", "user");
//            attrs.put("samAccountName", "AlbertE");
//            attrs.put("cn", "Albert Einstein");
//
//            //These are some optional (but useful) attributes
//            attrs.put("giveName", "Albert");
//            attrs.put("sn", "Einstein");
//            attrs.put("displayName", "Albert Einstein");
//            attrs.put("description", "Research Scientist");
//            attrs.put("userPrincipalName", "AlbertE@antipodes.com");
//            attrs.put("mail", "relativity@antipodes.com");
//            attrs.put("telephoneNumber", "999 123 4567");
//
//            //some useful constants from lmaccess.h
//            int UF_ACCOUNTDISABLE = 0x0002;
//            int UF_PASSWD_NOTREQD = 0x0020;
//            int UF_PASSWD_CANT_CHANGE = 0x0040;
//            int UF_NORMAL_ACCOUNT = 0x0200;
//            int UF_DONT_EXPIRE_PASSWD = 0x10000;
//            int UF_PASSWORD_EXPIRED = 0x800000;
//
//            //Note that you need to create the user object before you can
//            //set the password. Therefore as the user is created with no 
//            //password, user AccountControl must be set to the following
//            //otherwise the Win2K3 password filter will return error 53
//            //unwilling to perform.
//            attrs.put("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED + UF_ACCOUNTDISABLE));
//
//            // Create the context
//            Context result = ctx.createSubcontext(userName, attrs);
//            System.out.println("Created disabled account for: " + userName);
//
//            //now that we've created the user object, we can set the 
//            //password and change the userAccountControl
//            //and because password can only be set using SSL/TLS
//            //lets use StartTLS
//            StartTlsResponse tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
//            tls.negotiate();
//
//            //set password is a ldap modfy operation
//            //and we'll update the userAccountControl
//            //enabling the acount and force the user to update ther password
//            //the first time they login
//            ModificationItem[] mods = new ModificationItem[2];
//
//            //Replace the "unicdodePwd" attribute with a new value
//            //Password must be both Unicode and a quoted string
//            String newQuotedPassword = "\"Password2000\"";
//            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
//
//            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
//            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl", Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWORD_EXPIRED)));
//
//            // Perform the update
//            ctx.modifyAttributes(userName, mods);
//            System.out.println("Set password & updated userccountControl");
//
//            //now add the user to a group.
//            try {
//                ModificationItem member[] = new ModificationItem[1];
//                member[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", userName));
//
//                ctx.modifyAttributes(groupName, member);
//                System.out.println("Added user to group: " + groupName);
//
//            } catch (NamingException e) {
//                System.err.println("Problem adding user to group: " + e);
//            }
//            //Could have put tls.close()  prior to the group modification
//            //but it seems to screw up the connection  or context ?
//            tls.close();
//            ctx.close();
//
//            System.out.println("Successfully created User: " + userName);
//
//        } catch (NamingException e) {
//            System.err.println("Problem creating object: " + e);
//        } catch (IOException e) {
//            System.err.println("Problem creating object: " + e);
//        }
// TODO code application logic here
//        System.out.println("**********************");
//        InfoGroupUser a = new InfoGroupUser();
//        AddUser c = new AddUser("abdullah", "abdullah", "khan", "Abdullah#khan", "amu_users");
//        
//        if(c.addUser()){
//            System.out.println("User added");
//        } else{
//            System.out.println("User not added");
//        }
//        ArrayList<String> aa = a.f_UserGroup("amir");
//        System.out.println("\n************************************");
//        System.out.println("*****Information user bob  *********");
//        System.out.println("************************************");
//        for (int i = 0; i < aa.size(); i++) {
//            System.out.println(aa.get(i));
//        }
//
//        System.out.println("\n************************************");
//        System.out.println("*****  Information Group  *********");
//        System.out.println("************************************");
//        ArrayList<String> bb = a.f_AllGroup();
//        for (int i = 0; i < bb.size(); i++) {
//            System.out.println(bb.get(i));
//        }
}
