/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad;

import java.security.Security;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;

public class ADConnection {

    DirContext ldapContext;
    String baseName = "cn=Mohammad Amir,cn=users,DC=sdc,DC=local";
    String serverIP = "10.0.6.84";

    public ADConnection() {
        try {
            Hashtable ldapEnv = new Hashtable(11);
            ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnv.put(Context.PROVIDER_URL, "ldap://" + serverIP + ":636");
            ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
            ldapEnv.put(Context.SECURITY_PRINCIPAL, "cn=administrator" + baseName);
            ldapEnv.put(Context.SECURITY_CREDENTIALS, "Amber#lari");
            ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");
            ldapContext = new InitialDirContext(ldapEnv);
        } catch (Exception e) {
            System.out.println(" bind error: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void updatePassword(String username, String password) {
        try {
            String quotedPassword = "\"" + password + "\"";
            char unicodePwd[] = quotedPassword.toCharArray();
            byte pwdArray[] = new byte[unicodePwd.length * 2];
            for (int i = 0; i < unicodePwd.length; i++) {
                pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
                pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
            }
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute("UnicodePwd", pwdArray));
            ldapContext.modifyAttributes("cn=" + username + baseName, mods);
        } catch (Exception e) {
            System.out.println("update password error: " + e);
            System.exit(-1);
        }
    }    
}
