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
import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class InfoGroupUser {

    // User name
    public final String adminName = "CN=Mohammad Amir,CN=Users,DC=sdc,DC=local"; // CN=Alice,CN=Users,DC=DEMO,DC=COM

    // Password
    public final String adminPassword = "Amber#lari"; // password admin
    // Address login AD
    public final String ldapURL = "ldap://10.0.6.84:3268";

    // Get information group by userName
    public ArrayList<String> f_UserGroup(String userName) {

        ArrayList arrayUserGroup = new ArrayList();

        // Connect Active directory
        Hashtable env = new Hashtable();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // set security credentials, note using simple cleartext authentication
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);
        env.put(Context.SECURITY_CREDENTIALS, adminPassword);

        // connect to my domain controller
        env.put(Context.PROVIDER_URL, ldapURL);
        // specify attributes to be returned in binary format
        env.put("java.naming.ldap.attributes.binary", "tokenGroups");

        try {

            // Create the initial directory context
            LdapContext ctx = new InitialLdapContext(env, null);

            final String ldapSearchBase = "dc=sdc,dc=local"; 	// Domain
            SearchResult srLdapUser = findAccountByAccountName(ctx,
                    ldapSearchBase, userName);

            System.out.println(srLdapUser.getName());

            // Create the search controls
            SearchControls userSearchCtls = new SearchControls();

            // Specify the search scope
            userSearchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);

            // specify the LDAP search filter to find the user in question
            String userSearchFilter = "(objectClass=user)";

            // paceholder for an LDAP filter that will store SIDs of the groups
            // the user belongs to
            StringBuffer groupsSearchFilter = new StringBuffer();
            groupsSearchFilter.append("(|");

            // Specify the Base for the search
            // String userSearchBase = "CN= " + userName +
            // ",CN=Users,DC=demo,DC=com"; // bob
            String userSearchBase = srLdapUser.getName() + ",DC=sdc,DC=local"; // Domain
            // Specify the attributes to return
            String userReturnedAtts[] = {"tokenGroups"};
            userSearchCtls.setReturningAttributes(userReturnedAtts);

            // Search for objects using the filter
            NamingEnumeration userAnswer = ctx.search(userSearchBase,
                    userSearchFilter, userSearchCtls);

            // Loop through the search results
            while (userAnswer.hasMoreElements()) {

                SearchResult sr = (SearchResult) userAnswer.next();
                Attributes attrs = sr.getAttributes();

                if (attrs != null) {

                    try {
                        for (NamingEnumeration ae = attrs.getAll(); ae
                                .hasMore();) {
                            Attribute attr = (Attribute) ae.next();
                            for (NamingEnumeration e = attr.getAll(); e
                                    .hasMore();) {

                                byte[] sid = (byte[]) e.next();
                                groupsSearchFilter.append("(objectSid="
                                        + binarySidToStringSid(sid) + ")");

                            }
                            groupsSearchFilter.append(")");
                        }

                    } catch (NamingException e) {
                        System.err.println("Problem listing membership: " + e);
                    }
                }
            }

            // Search for groups the user belongs to in order to get their names
            // Create the search controls
            SearchControls groupsSearchCtls = new SearchControls();

            // Specify the search scope
            groupsSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            // Specify the Base for the search
            String groupsSearchBase = "DC=sdc,DC=local"; // Domain

            // Specify the attributes to return
            String groupsReturnedAtts[] = {"sAMAccountName"};

            groupsSearchCtls.setReturningAttributes(groupsReturnedAtts);

            // Search for objects using the filter
            NamingEnumeration groupsAnswer = ctx.search(groupsSearchBase,
                    groupsSearchFilter.toString(), groupsSearchCtls);

            // Loop through the search results
            while (groupsAnswer.hasMoreElements()) {

                SearchResult sr = (SearchResult) groupsAnswer.next();
                Attributes attrs = sr.getAttributes();

                if (attrs != null) {
                    String nameUserGroup = attrs.get("sAMAccountName").get()
                            .toString();
                    {
                        //System.out.println(nameUserGroup);
                        arrayUserGroup.add(nameUserGroup);
                    }
                }
            }

            ctx.close();

        } catch (NamingException e) {
            System.err.println("Problem searching directory: " + e);
            return null;
        }

        return arrayUserGroup;
    }

    public ArrayList<String> f_AllGroup() {

        ArrayList arrayUserGroup = new ArrayList();

        String usersContainer = "";

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);
        env.put(Context.SECURITY_PRINCIPAL, adminName);
        env.put(Context.SECURITY_CREDENTIALS, adminPassword);

        try {
            DirContext context = new InitialDirContext(env);
            SearchControls searchControls = new SearchControls();
            String[] attrIDs = {"cn"};
            searchControls.setReturningAttributes(attrIDs);
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration answer = context.search(usersContainer,
                    "(objectclass=group)", searchControls); // group
            while (answer.hasMore()) {
                SearchResult rslt = (SearchResult) answer.next();
                javax.naming.directory.Attributes attrs = rslt.getAttributes();
                String groupName = attrs.get("cn").toString();
                //System.out.println(attrs.get("cn"));
                arrayUserGroup.add(groupName);
            }

            context.close();

        } catch (NamingException e) {
            e.printStackTrace();
            return null;
        }

        return arrayUserGroup;
    }

    public static final String binarySidToStringSid(byte[] SID) {

        String strSID = "";

        // convert the SID into string format
        long version;
        long authority;
        long count;
        long rid;

        strSID = "S";
        version = SID[0];
        strSID = strSID + "-" + Long.toString(version);
        authority = SID[4];

        for (int i = 0; i < 4; i++) {
            authority <<= 8;
            authority += SID[4 + i] & 0xFF;
        }

        strSID = strSID + "-" + Long.toString(authority);
        count = SID[2];
        count <<= 8;
        count += SID[1] & 0xFF;

        for (int j = 0; j < count; j++) {

            rid = SID[11 + (j * 4)] & 0xFF;

            for (int k = 1; k < 4; k++) {

                rid <<= 8;

                rid += SID[11 - k + (j * 4)] & 0xFF;

            }

            strSID = strSID + "-" + Long.toString(rid);

        }

        return strSID;

    }

    public SearchResult findAccountByAccountName(DirContext ctx,
            String ldapSearchBase, String accountName) throws NamingException {

        String searchFilter = "(&(objectClass=user)(sAMAccountName=" + accountName + "))";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase,
                searchFilter, searchControls);

        SearchResult searchResult = null;
        if (results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();

            // make sure there is not another item available, there should be
            // only 1 match
            if (results.hasMoreElements()) {
                System.err
                        .println("Matched multiple users for the accountName: "
                                + accountName);
                return null;
            }
        }
        return searchResult;
    }
}
