package fr.ambuconnect.messagerie.ressources;

import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import fr.ambuconnect.messagerie.dto.ConversationDTO;
import fr.ambuconnect.messagerie.dto.ErrorDTO;
import fr.ambuconnect.messagerie.dto.MessagerieDto;
import fr.ambuconnect.messagerie.dto.UserStatusDTO;
import fr.ambuconnect.messagerie.mapper.MessagerieMapper;
import fr.ambuconnect.messagerie.services.MessagerieService;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

@Path("/message")
@ServerEndpoint("/chat/{userId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessagerieRessource {

    private final MessagerieMapper messagerieMapper;
    private final ObjectMapper objectMapper;
    private final MessagerieService messagerieService;

    @Inject
    public MessagerieRessource(MessagerieMapper messagerieMapper, ObjectMapper objectMapper, MessagerieService messagerieService){
        this.messagerieMapper =  messagerieMapper;
        this.objectMapper = objectMapper;
        this.messagerieService = messagerieService;
    }

    private Map<UUID, Session> sessions = new HashMap<>();

    private static final Logger logger = Logger.getLogger(MessagerieRessource.class.getName());

    @POST
    public Response createMessage(MessagerieDto MessagerieDto) {
        MessagerieDto created = messagerieService.createMessage(MessagerieDto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public MessagerieDto getMessage(@PathParam("id") UUID id) {
        return messagerieService.getMessageById(id);
    }

    @GET
    @Path("/conversation")
    public List<MessagerieDto> getConversation(
            @QueryParam("expediteur") UUID expediteurId,
            @QueryParam("destinataire") UUID destinataireId) {
        return messagerieService.getConversation(expediteurId, destinataireId);
    }

    @GET
    @Path("/course/{courseId}")
    public List<MessagerieDto> getMessagesByCourse(@PathParam("courseId") UUID courseId) {
        return messagerieService.getMessagesByCourse(courseId);
    }

    @GET
    @Path("/destinataire/{destinataireId}")
    public List<MessagerieDto> getMessagesByDestinataire(@PathParam("destinataireId") UUID destinataireId) {
        return messagerieService.getMessagesByDestinataire(destinataireId);
    }

    @GET
    @Path("/expediteur/{expediteurId}")
    public List<MessagerieDto> getMessagesByExpediteur(@PathParam("expediteurId") UUID expediteurId) {
        return messagerieService.getMessagesByExpediteur(expediteurId);
    }

    @PUT
    @Path("/{id}")
    public MessagerieDto updateMessage(@PathParam("id") UUID id, MessagerieDto MessagerieDto) {
        return messagerieService.updateMessage(id, MessagerieDto);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMessage(@PathParam("id") UUID id) {
        messagerieService.deleteMessage(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/read")
    public MessagerieDto markMessageAsRead(@PathParam("id") UUID id) {
        return messagerieService.markMessageAsRead(id);
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        sessions.put(userId, session);
        logger.info("Nouvelle connexion WebSocket - UserId: " + userId);
        logger.info("Sessions actives: " + sessions.size());
        logger.info("Liste des utilisateurs connectés: " + sessions.keySet());
        broadcastUserStatus(userId, "CONNECTED");
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        sessions.remove(userId);
        broadcastUserStatus(userId, "DISCONNECTED");
    }

    @OnError
    public void onError(Session session, @PathParam("userId") String userIdString, Throwable throwable) {
        UUID userId = UUID.fromString(userIdString);
        sessions.remove(userId);
        broadcastUserStatus(userId, "ERROR");
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String type = jsonNode.get("type").asText();
            
            if ("GET_CONVERSATION".equals(type)) {
                UUID otherUserId = UUID.fromString(jsonNode.get("otherUserId").asText());
                List<MessagerieDto> conversation = messagerieService.getConversation(userId, otherUserId);
                
                Session userSession = sessions.get(userId);
                if (userSession != null && userSession.isOpen()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "CONVERSATION");
                    response.put("messages", conversation);
                    userSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(response));
                }
            } else {
                logger.info("Message reçu de l'utilisateur: " + userId);
                logger.info("Contenu du message: " + message);
                
                try {
                    MessagerieDto MessagerieDto = objectMapper.readValue(message, MessagerieDto.class);
                    logger.info("Message parsé - De: " + MessagerieDto.getExpediteurId() + " À: " + MessagerieDto.getDestinataireId());
                    
                    // Sauvegarder le message
                    MessagerieDto = messagerieService.createMessage(MessagerieDto);
                    
                    // Envoyer le message à l'expéditeur aussi
                    sendMessageToUser(MessagerieDto, MessagerieDto.getDestinataireId());
                    sendMessageToUser(MessagerieDto, MessagerieDto.getExpediteurId());
                    
                } catch (Exception e) {
                    logger.severe("Erreur lors du traitement du message: " + e.getMessage());
                    handleError(userId, e);
                }
            }
        } catch (Exception e) {
            handleError(userId, e);
        }
    }

    private void sendMessageToUser(MessagerieDto MessagerieDto, UUID userId) {
        logger.info("Tentative d'envoi de message à l'utilisateur: " + userId);
        try {
            Session userSession = sessions.get(userId);
            if (userSession != null && userSession.isOpen()) {
                String messageJson = objectMapper.writeValueAsString(MessagerieDto);
                logger.info("Session trouvée et ouverte pour l'utilisateur: " + userId);
                userSession.getAsyncRemote().sendText(messageJson);
                logger.info("Message envoyé avec succès à: " + userId);
            } else {
                logger.warning("Session non trouvée ou fermée pour l'utilisateur: " + userId);
                if (userSession == null) {
                    logger.warning("Session est null");
                } else {
                    logger.warning("Session fermée: isOpen = " + userSession.isOpen());
                }
            }
        } catch (Exception e) {
            logger.severe("Erreur lors de l'envoi du message à " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour diffuser le statut d'un utilisateur
    private void broadcastUserStatus(UUID userId, String status) {
        UserStatusDTO statusDTO = new UserStatusDTO(userId, status);
        try {
            String statusJson = objectMapper.writeValueAsString(statusDTO);
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(statusJson);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleError(UUID userId, Exception e) {
        try {
            Session session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                ErrorDTO errorDTO = new ErrorDTO("ERROR", e.getMessage());
                session.getAsyncRemote().sendText(
                    objectMapper.writeValueAsString(errorDTO)
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @GET
    @Path("/conversations/{userId}")
    public Response getUserConversations(@PathParam("userId") UUID userId) {
        try {
            List<ConversationDTO> conversations = messagerieService.getUserConversations(userId);
            return Response.ok(conversations).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de la récupération des conversations: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/conversation/message")
    public Response sendMessage(MessagerieDto messageDto) {
        try {
            MessagerieDto sent = messagerieService.sendMessageChauffeurAdmin(messageDto);
            return Response.status(Response.Status.CREATED).entity(sent).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur lors de l'envoi du message: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/debug/sessions")
    public Response getSessionsStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("nombreSessions", sessions.size());
        status.put("utilisateursConnectes", sessions.keySet());
        
        Map<String, Boolean> sessionStatuses = new HashMap<>();
        sessions.forEach((userId, session) -> 
            sessionStatuses.put(userId.toString(), session.isOpen())
        );
        status.put("statutSessions", sessionStatuses);
        
        return Response.ok(status).build();
    }
}