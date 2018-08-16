package Controller;

import Service.ContactService;
import spark.Request;

import static spark.Spark.*;

public class ContactController {

    public ContactController(final ContactService contactService) {
        get("/hello", (req, res) -> "Hello World");
        get("/hello/:name", (req,res)->{
            return "Hello, "+ req.params(":name");
        });
        //post("/contacts", "application/json", (req, res) -> createContact(req));
    }


}
