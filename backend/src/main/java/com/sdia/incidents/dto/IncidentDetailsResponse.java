package com.sdia.incidents.dto;

import com.sdia.incidents.entity.Incident;

import java.util.List;

/** Vue "detail" : incident + historique + notes de traitement. */
public record IncidentDetailsResponse(
        IncidentResponse incident,
        List<HistoryResponse> history,
        List<CommentResponse> comments
) {
    public static IncidentDetailsResponse from(Incident i) {
        return new IncidentDetailsResponse(
                IncidentResponse.from(i),
                i.getHistory().stream().map(HistoryResponse::from).toList(),
                i.getComments().stream().map(CommentResponse::from).toList());
    }
}
