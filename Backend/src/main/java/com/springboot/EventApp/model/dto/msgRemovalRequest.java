package com.springboot.EventApp.model.dto;

/**
 * This is used to send a message to remove a chat from the user side (for moderation purposes)
 *
 * @author Lucas Horn
 */
public class msgRemovalRequest {
    Long msgIdToDelete;

    public msgRemovalRequest() {
    }

    public msgRemovalRequest(Long msgIdToDelete) {
        this.msgIdToDelete = msgIdToDelete;
    }

    public Long getMsgIdToDelete() {
        return msgIdToDelete;
    }

    public void setMsgIdToDelete(Long msgIdToDelete) {
        this.msgIdToDelete = msgIdToDelete;
    }
}
