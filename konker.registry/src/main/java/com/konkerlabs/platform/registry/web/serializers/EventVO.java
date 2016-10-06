package com.konkerlabs.platform.registry.web.serializers;

import com.fasterxml.jackson.annotation.JsonView;
import com.konkerlabs.platform.registry.business.model.Event;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 06/10/16.
 */
@Data
@Builder
public class EventVO {

    @JsonView(EventJsonView.class)
    private EventMeta meta;

    @JsonView(EventJsonView.class)
    private String data;


    @Data
    @Builder
    public static class EventMeta {
        @JsonView(EventJsonView.class)
        private Long timestamp;

        @JsonView(EventJsonView.class)
        private Event.EventActor incoming;

        @JsonView(EventJsonView.class)
        private Event.EventActor outgoing;
    }

    public static List<EventVO> from(List<Event> events) {
        List<EventVO> items = new ArrayList<>();
        events.stream().forEach(item -> {
            items.add(EventVO.builder()
                    .meta(EventVO.EventMeta.builder()
                            .incoming(item.getIncoming())
                            .outgoing(item.getOutgoing())
                            .timestamp(item.getTimestamp().toEpochMilli())
                            .build()
                    )
                    .data(item.getPayload())
                    .build());
        });
        return items;
    }
}
