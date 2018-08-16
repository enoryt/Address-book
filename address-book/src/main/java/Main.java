import Controller.ContactController;
import Service.ContactService;
import model.Contact;
import org.elasticsearch.common.Strings;
import spark.Request;

import java.util.List;
import java.util.Set;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {

        post("/contacts", "application/json", (req, res) -> createContact(req));
        get("/contacts/:name", "application/json", (req, res) -> getContact(req));
        delete("/contacts/:name", "application/json", (req, res) -> deleteContact(req));
        put("/contacts/:name", "application/json", (req, res) -> updateContact(req));
        get("/contacts", "application/json", (req, res) -> getAllContacts(req));

        //new ContactController(new ContactService());
    }
    private static String createContact(Request req) {
        String success = ContactService.getInstance().createContact(req);
        return success;
    }
    private static Contact getContact(Request req){
        Contact c= new Contact();
        String name = req.params(":name");
        c=ContactService.getInstance().getContact(name);
        if(c==null){
            System.out.println("Contact not found");
            return c;
        }
        System.out.println("Contact found: "+ c.getFirstName());
        return c;
    }

    private static String deleteContact(Request req) {

        String name = req.params(":name");
        System.out.println("Atempting to delete "+name);
        boolean result = ContactService.getInstance().deleteContact(name);
        if(result==true) System.out.println("Deletion successful");
        else System.out.println("Deletion failed");
        return result? "Success" : "Failure";
    }
    private static String updateContact(Request req) {
        boolean result = ContactService.getInstance().updateContact(req);

        return result? "Success" : "Failure";

    }
    private static List<Contact> getAllContacts(Request req) {
        int pageSize =0;
        int pageNumber = 0;
        String queryStringQuery = null;
        Set<String> queryParams = req.queryParams();
        try {
            if (queryParams.contains("pageSize")) {
                pageSize = Integer.parseInt(req.queryParams("pageSize"));
            }
            else pageSize=5; //some default value

        } catch (Exception e) {pageSize=5;}

        try {
            if (queryParams.contains("page")) {
                pageNumber = Integer.parseInt(req.queryParams("page"));
            }
        } catch (Exception e) {pageNumber=0;}

        if (queryParams.contains("query"))
            queryStringQuery = req.queryParams("query");


        return ContactService.getInstance().getAllContacts(pageSize, pageNumber, queryStringQuery);

    }
}
