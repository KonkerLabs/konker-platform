package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Document(collection = "tenants")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tenant implements URIDealer, Serializable {

    @Id
    private String id;
    private String name;
    private String domainName;
    private LogLevel logLevel = LogLevel.WARNING;
    private Long devicesLimit;
    private Long privateStorageSize;
    private boolean chargeable;
    private PlanEnum plan;

    public static final String URI_SCHEME = "tenant";

    @Override
    public String getUriScheme() {
        return URI_SCHEME;
    }

    @Override
    public String getContext() {
        return domainName;
    }

    @Override
    public String getGuid() {
        return id;
    }

	public LogLevel getLogLevel() {
		return Optional.ofNullable(logLevel).orElse(LogLevel.WARNING);
	}

    public enum PlanEnum {
        EDUCATION("Education",
                Arrays.asList("R$ 1,00/dia","Dispositivos ilimitados","1 usuário","1 aplicação","Input: 5 mensagens/minuto","Output: 50 Megabytes/dia")),
        STARTUP("Startup",
                Arrays.asList("R$ 3,00/dia","Dispositivos ilimitados","100 usuário","5 aplicações","Input: 10 mensagens/minuto","Output: 100 Megabytes/dia")),
        GROWTH("Growth",
                Arrays.asList("R$ 10,00/dia","Dispositivos ilimitados","1000 usuário","50 aplicações","Input: 60 mensagens/minuto","Output: 1 Gigabyte/dia")),
        STARTER("Starter",
                Arrays.asList("Dispositivos ilimitados","R$ 2,00/mês por dispositivo","Usuário ilimitados","Aplicações ilimitadas","Input: 120 mensagens/minuto","Output: 5 Gigabyte/dia",
                "R$ 0,048/Megabyte armazenado")),
        STANDARD("Standard",
                Arrays.asList("Dispositivos ilimitados","R$ 5,00/mês por dispositivo","Usuário ilimitados","Aplicações ilimitadas","Input: 240 mensagens/minuto","Output: 10 Gigabyte/dia",
                        "R$ 0,096/Megabyte armazenado")),
        CORPORATE("Corporate",
                Arrays.asList("Dispositivos ilimitados","R$ 15,00/mês por dispositivo","Usuário ilimitados","Aplicações ilimitadas","Input: ilimitado","Output: ilimitado",
                        "R$ 0,144/Megabyte armazenado")),
        ENTERPRISE("Enterprise",
                Arrays.asList("Dispositivos ilimitados","Usuário ilimitados","Aplicações ilimitadas","Sob consulta"));

        public String getValue() {
            return value;
        }

        public List<String> getConditions() {
            return conditions;
        }

        private String value;
        private List<String> conditions;

        PlanEnum(String value, List<String> conditions) {
            this.value = value;
            this.conditions = conditions;
        }
    }

}
