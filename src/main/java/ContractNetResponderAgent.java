import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import org.json.JSONObject;

public class ContractNetResponderAgent extends Agent {
    private String baseUrl = "http://api.weatherstack.com/current";
    private String apiKey = "7a1ed3b9b1dda56a944d8e4d436b6b7a";

    protected void setup() {
        System.out.println("L'agente " + getLocalName() + " e' in attesa di una CFP...");
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new MyContractNetResponder(this, template));
    }

    private class MyContractNetResponder extends ContractNetResponder {

        public MyContractNetResponder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) {
            System.out.println("Agente " + getLocalName() + ": Ho ricevuto una cfp da " + cfp.getSender().getLocalName()
                    + " { id: " + cfp.getConversationId() + ", content: " + cfp.getContent() + " }");

            String city = cfp.getContent();

            String forecast = getForecast(city);
            ACLMessage propose = cfp.createReply();
            if (forecast != null) {
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(forecast);
            } else {
                propose.setPerformative(ACLMessage.REFUSE);
            }
            return propose;
        }

        private String getForecast(String city) {
            String apiUrl = baseUrl + "?access_key=" + apiKey + "&query=" + city;
            JSONObject json = JsonReader.readJsonFromUrl(apiUrl);

            return (json != null && json.has("request") ? buildForecastString(json) : null);
        }

        private String buildForecastString(JSONObject json) {
            JSONObject location = json.getJSONObject("location");
            String place = location.getString("name");
            String region = location.getString("region");
            String country = location.getString("country");
            String localtime = location.getString("localtime");

            JSONObject current = json.getJSONObject("current");
            int temperature = current.getInt("temperature");
            int perceived = current.getInt("feelslike");
            int windspeed = current.getInt("wind_speed");
            int cloudcover = current.getInt("cloudcover");
            int humidity = current.getInt("humidity");
            String weather = current.getJSONArray("weather_descriptions").getString(0);

            return "=".repeat(100) + "\n" + place + ", " + region + " (" + country + ") - " + localtime +
                    "\n- Weather: " + weather +
                    "\n- Temperature: " +
                    "\n\t- Real: " + temperature + " celsius" +
                    "\n\t- Perceived: " + perceived + " celsius" +
                    "\n- Wind speed: " + windspeed + "km/h" +
                    "\n- Cloud covering: " + cloudcover + "%" +
                    "\n- Humidity: " + humidity + "%" +
                    "\n" + "=".repeat(100);

        }

    }

}
