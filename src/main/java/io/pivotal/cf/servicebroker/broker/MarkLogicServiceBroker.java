package io.pivotal.cf.servicebroker.broker;

import io.pivotal.cf.servicebroker.model.ServiceBinding;
import io.pivotal.cf.servicebroker.model.ServiceInstance;
import io.pivotal.cf.servicebroker.service.DefaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Example service broker. Can be used as a template for creating custom service brokers
 * by adding your code in the appropriate methods. For more information on the CF service broker
 * lifecycle and API, please see See <a href="https://docs.cloudfoundry.org/services/api.html">here.</a>
 * <p>
 * This class extends DefaultServiceImpl, which has no-op implementations of the methods. This means
 * that if, for instance, your broker does not support binding you can just delete the binding methods below
 * (in other words, you do not need to implement your own no-op implementations).
 */
@Service
@Slf4j
public class MarkLogicServiceBroker extends DefaultServiceImpl {

    @Autowired
    private MarkLogicManageAPI markLogicManageAPI;

    @Autowired
    private Environment env;

    /**
     * Add code here and it will be run during the create-service process. This might include
     * calling back to your underlying service to create users, schemas, fire up environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the create-service request, which will show up as key value pairs in instance.parameters.
     * @throws ServiceBrokerException thrown this for any errors during instance creation.
     */
    @Override
    public void createInstance(ServiceInstance instance) throws ServiceBrokerException {
        log.info("creating instance with id: " + instance.getId());

        // create content DB
        Map<String, String> m = new HashMap<>();
        m.put("database-name", instance.getId() + "-content");

        log.info("creating content database");
        markLogicManageAPI.createDatabase(m);

        m.clear();
        m.put("database-name", instance.getId() + "-modules");

        log.info("creating modules database");
        markLogicManageAPI.createDatabase(m);

        m.clear();
        m.put("forest-name", instance.getId() + "-content-001-1");
        m.put("host", env.getProperty("ML_CLUSTER_NAME"));
        m.put("database", instance.getId() + "-content");

        log.info("creating content forrest");
        markLogicManageAPI.createForest(m);

        m.clear();
        m.put("forest-name", instance.getId() + "-modules-001-1");
        m.put("host", env.getProperty("ML_CLUSTER_NAME"));
        m.put("database", instance.getId() + "-modules");

        log.info("creating modules forrest");
        markLogicManageAPI.createForest(m);
    }

    /**
     * Code here will be called during the delete-service instance process. You can use this to de-allocate resources
     * on your underlying service, delete user accounts, destroy environments, etc.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion.
     */
    @Override
    public void deleteInstance(ServiceInstance instance) throws ServiceBrokerException {
        //TODO ml db clean up and destroy db and forests

        // delete content DB
        String databaseDelete = "database-name" + instance.getId() + "-content";
        markLogicManageAPI.deleteDatabase(databaseDelete);

        // delete modules DB
        databaseDelete = "database-name" + instance.getId() + "-modules";
        markLogicManageAPI.deleteDatabase(databaseDelete);

        // delete content Forest
        String forestDelete = "forest-name" + instance.getId() + "-content-001-1";

        markLogicManageAPI.deleteForest(forestDelete);

        //delete modules Forest
        forestDelete = "forest-name" + instance.getId() + "-modules-001-1";

        markLogicManageAPI.deleteForest(forestDelete);
    }

    /**
     * Code here will be called during the update-service process. You can use this to modify
     * your service instance.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during instance deletion. Services that do not support
     *                                updating can through ServiceInstanceUpdateNotSupportedException here.
     */
    @Override
    public void updateInstance(ServiceInstance instance) throws ServiceBrokerException {
        //TODO add more forests, nodes, indexes.....
    }

    /**
     * Called during the bind-service process. This is a good time to set up anything on your underlying service specifically
     * needed by an application, such as user accounts, rights and permissions, application-specific environments and connections, etc.
     * <p>
     * Services that do not support binding should set '"bindable": false,' within their catalog.json file. In this case this method
     * can be safely deleted in your implementation.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector. Clients can pass additional json
     *                 as part of the bind-service request, which will show up as key value pairs in binding.parameters. Brokers
     *                 can, as part of this method, store any information needed for credentials and unbinding operations as key/value
     *                 pairs in binding.properties
     * @throws ServiceBrokerException thrown this for any errors during binding creation.
     */
    @Override
    public void createBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO create the binding via API... Create Roles, Users with those roles and passwords.

        //create role in Security DB
        Map<String, String> m = new HashMap<>();
        m.put("role-name", instance.getId() + "-admin-role");
        markLogicManageAPI.createRole(m);

        m.clear();

        String pw = UUID.randomUUID().toString();

        //create user in Security DB
        m.put("user-name", instance.getId() + "-admin");
        m.put("password", pw);
        m.put("description", instance.getId() + " admin user");
        m.put("role", "[" + instance.getId() + "-admin-role]");
        markLogicManageAPI.createRole(m);

        binding.getParameters().putAll(m);
    }

    /**
     * Called during the unbind-service process. This is a good time to destroy any resources, users, connections set up during the bind process.
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @throws ServiceBrokerException thrown this for any errors during the unbinding creation.
     */
    @Override
    public void deleteBinding(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO call API to delete stuff
    }

    /**
     * Bind credentials that will be returned as the result of a create-binding process. The format and values of these credentials will
     * depend on the nature of the underlying service. For more information and some examples, see
     * <a href=https://docs.cloudfoundry.org/services/binding-credentials.html>here.</a>
     * <p>
     * This method is called after the create-binding method: any information stored in binding.properties in the createBinding call
     * will be availble here, along with any custom data passed in as json parameters as part of the create-binding process by the client).
     *
     * @param instance service instance data passed in by the cloud connector.
     * @param binding  binding data passed in by the cloud connector.
     * @return credentials, as a series of key/value pairs
     * @throws ServiceBrokerException thrown this for any errors during credential creation.
     */
    @Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) throws ServiceBrokerException {
        //TODO Put together the VCAP_Services-type variables that are needed. Maybe use the java connection library later.

        Map<String, Object> m = new HashMap<>();
        m.put("username", binding.getParameters().get("username"));
        m.put("password", binding.getParameters().get("password"));

        //maybe something like this? Are the host/port etcs same as we get from the application.props file?
        //or do they come from the backend service somehow?
//        m.put("host", host);
//        m.put("port", port);
//        m.put("database", clusterName);
//
//        String uri = "http://" + m.get("username") + ":" + m.get("password") + "@" + m.get("host") + ":" + m.get("port") + "/" + m.get("database");
//
//        m.put("uri", uri);

        return m;
    }

    @Override
    //TODO deal with async
    public boolean isAsynch() {
        return false;
    }
}