import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class WeatherAgent extends Agent {
    private final String[] apiUrls = {"http://api.weatherapi.com/v1/current.json?key=f5e52cc5fd814e1f8fc142333212606&q=",
            "http://api.weatherstack.com/current?access_key=7a1ed3b9b1dda56a944d8e4d436b6b7a&query="};

    protected void setup() {
        Object[] args = getArguments();
        int apiType = Integer.parseInt((String) args[0]);
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new MyContractNetResponder(this, template, apiUrls[apiType]));
    }

    private static class MyContractNetResponder extends ContractNetResponder {
        private String city;
        private JSONObject weather;
        private final String apiUrl;

        public MyContractNetResponder(Agent a, MessageTemplate mt, String apiUrl) {
            super(a, mt);
            this.apiUrl = apiUrl;
        }

        protected void agentLog(String msg) {
            System.out.println(myAgent.getLocalName() + ": " + msg);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) {
            agentLog("Ho ricevuto una cfp da " + cfp.getSender().getLocalName() + ", content: " + cfp.getContent() + " }");
            city = cfp.getContent();

            // il tempo risultante dalla GET viene usato come costo
            long start_time = System.nanoTime();
            Boolean agentHasCity = getWeather();
            long end_time = System.nanoTime();

            ACLMessage propose = cfp.createReply();
            if (agentHasCity) { // l'agente ha trovato il meteo della città
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf((end_time - start_time) / 1e6));
            } else { // l'agente non ha trovato il meteo della città
                propose.setPerformative(ACLMessage.REFUSE);
            }
            return propose;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            ACLMessage msg = accept.createReply();
            try {
                dumpWeatherJson();
                msg.setPerformative(ACLMessage.INFORM);
                msg.setContent("true");
            } catch (IOException e) {
                msg.setPerformative(ACLMessage.FAILURE);
                msg.setContent(e.getMessage());
            }
            return msg;
        }

        private void dumpWeatherJson() throws IOException {
            FileWriter file = new FileWriter("weather.json");
            file.write(weather.toString(4));
            file.flush();
            file.close();
        }

        private Boolean getWeather() {
            weather = JsonReader.readJsonFromUrl(apiUrl + city);
            return weather != null && !weather.has("error") && !weather.has("success");
        }
    }

}
