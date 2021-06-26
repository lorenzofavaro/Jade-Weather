import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import org.json.JSONObject;

public class ContractNetResponderAgent extends Agent {
    private String baseUrl = "http://api.weatherapi.com/v1/current.json";
    private String apiKey = "f5e52cc5fd814e1f8fc142333212606";

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

            String weather = getWeather(city);
            ACLMessage propose = cfp.createReply();
            if (weather != null) {
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(weather);
            } else {
                propose.setPerformative(ACLMessage.REFUSE);
                propose.setContent("Citta' non trovata");
            }
            return propose;
        }

        private String getWeather(String city) {
            String apiUrl = baseUrl + "?key=" + apiKey + "&q=" + city;
            JSONObject json = JsonReader.readJsonFromUrl(apiUrl);

            return (json != null && !json.has("error") ? buildWeatherString(json) : null);
        }

        private String buildWeatherString(JSONObject json) {
            JSONObject location = json.getJSONObject("location");
            String place = location.getString("name");
            String region = location.getString("region");
            String country = location.getString("country");
            String localtime = location.getString("localtime");

            JSONObject current = json.getJSONObject("current");
            int temperature = current.getInt("temp_c");
            int perceived = current.getInt("feelslike_c");
            int windspeed = current.getInt("wind_kph");
            int cloudcover = current.getInt("cloud");
            int humidity = current.getInt("humidity");
            String weather = current.getJSONObject("condition").getString("text");

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
