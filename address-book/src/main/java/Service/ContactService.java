package Service;

import com.google.gson.Gson;
import model.Contact;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import spark.Request;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ContactService {
    private TransportClient client;
    private static ContactService thisInstance = new ContactService();

     private ContactService() {
        try {
            System.out.print("Hello from constructor");

            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
            System.out.println("Connected to elastic search host");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while connecting to elastic search host: e");
        }


    }

    public static ContactService getInstance() {
        return thisInstance;
    }

    public List<Contact> getAllContacts(int pageSize, int page, String queryString) {
        List<Contact> contacts = new ArrayList<>();
        SearchResponse response=client.prepareSearch("addressbook")
                .setTypes("contacts")
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(page * pageSize).setSize(pageSize).setExplain(true)
                .get();


        SearchHit[] hits = response.getHits().getHits();

        try {
            for (SearchHit hit : hits) {
                String s = hit.getSourceAsString();
                Contact c = new Gson().fromJson(s, Contact.class);
                System.out.println(c.getFirstName()+" "+c.getNumber()+" "+c.getEmail());
                contacts.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        return contacts;

    }

    public Contact getContact(String name) {

             System.out.println("Hello from get contact : " + name);
             GetResponse getResponse = client.prepareGet("addressbook", "contacts", name).get();
             System.out.println(getResponse.getSource());
             Contact contact = new Gson().fromJson(getResponse.getSourceAsString(), Contact.class);
             return contact;
    }

    public String createContact(Request req) {
            System.out.println("Hello from create");
            Contact c = new Gson().fromJson(req.body(), Contact.class);

            System.out.println("Test " + c.getFirstName());
            String name = c.getFirstName();

        if (getContact(name)==null) {
            try {

                XContentBuilder j = jsonBuilder();
                j.startObject();
                j.field("firstName", name);
                if (c.getEmail() != null) {
                    System.out.println(c.getEmail());
                    j.field("email", c.getEmail());
                }
                if (c.getNumber() != null) {
                    j.field("number", c.getNumber());
                }
                j.endObject();

                //string json= Strings.toString(j);
                //System.out.println("hello "+Strings.toString(j));
                IndexResponse response = client.prepareIndex("addressbook", "contacts", name).setSource(j).get();
                System.out.print("Contact added");
                return "Success";
            } catch (Exception e) {
                System.out.println(e);
                return "Failure";
            }
        }
        return "Failure";
    }

    public boolean deleteContact(String name) {

        DeleteResponse deleteResponse = client.prepareDelete("addressbook", "contacts", name).get();

        if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED) {
            return true;
        }

        return false;

    }
    public boolean updateContact(Request req) {
        Contact c = new Gson().fromJson(req.body(), Contact.class);
        System.out.println("Test " + c.getFirstName());
        String name = c.getFirstName();

        try{
            XContentBuilder j = jsonBuilder();
            j.startObject();
            j.field("firstName", name);
            if (c.getEmail() != null) {
                System.out.println(c.getEmail());
                j.field("email", c.getEmail());
            }
            if (c.getNumber() != null) {
                j.field("number", c.getNumber());
            }
            j.endObject();
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index("addressbook").type("contacts").id(name).doc(j);
            UpdateResponse updateResponse = client.update(updateRequest).get();
            if (updateResponse.status() == RestStatus.OK) {
                return true;
            }
        }catch(Exception e){
            System.out.println(e);
        }

        return false;
    }

}
