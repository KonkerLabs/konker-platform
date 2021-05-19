package com.konkerlabs.platform.registry.business.model;

import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Document(collection = "tenants")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tenant implements URIDealer, Serializable {

    private static final long serialVersionUID = -3837379204435103166L;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != ClassUtils.getUserClass(o.getClass())) return false;

        Tenant tenant = (Tenant) o;
        return name.equals(tenant.getName()) && domainName.equals(tenant.getDomainName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, domainName);
    }

	public enum PlanEnum {
        EDUCATION("Education",
                Arrays.asList("R$ 1,00/dia",
                        "R$ 30,00/mês",
                        "Dispositivos ilimitados",
                        "1 usuário",
                        "1 aplicação",
                        "Input: 5 mensagens/minuto",
                        "Output: 50 Megabytes/dia")),
        STARTUP("Startup",
                Arrays.asList("R$ 3,00/dia",
                        "R$ 90,00/mês",
                        "Dispositivos ilimitados",
                        "100 usuário",
                        "5 aplicações",
                        "Input: 10 mensagens/minuto",
                        "Output: 100 Megabytes/dia")),
        GROWTH("Growth",
                Arrays.asList("R$ 10,00/dia",
                        "R$ 300,00/mês",
                        "Dispositivos ilimitados",
                        "1000 usuário",
                        "50 aplicações",
                        "Input: 60 mensagens/minuto",
                        "Output: 1 Gigabyte/dia")),
        STARTER("Starter",
                Arrays.asList("R$ 2,00/mês por dispositivo",
                        "R$ 0,048/Megabyte armazenado",
                        "Dispositivos ilimitados",
                        "Usuário ilimitados",
                        "Aplicações ilimitadas",
                        "Input: 120 mensagens/minuto",
                        "Output: 5 Gigabyte/dia")),
        STANDARD("Standard",
                Arrays.asList("R$ 5,00/mês por dispositivo",
                        "R$ 0,096/Megabyte armazenado",
                        "Dispositivos ilimitados",
                        "Usuário ilimitados",
                        "Aplicações ilimitadas",
                        "Input: 240 mensagens/minuto",
                        "Output: 10 Gigabyte/dia")),
        CORPORATE("Corporate",
                Arrays.asList("R$ 15,00/mês por dispositivo",
                        "R$ 0,144/Megabyte armazenado",
                        "Dispositivos ilimitados",
                        "Usuário ilimitados",
                        "Aplicações ilimitadas",
                        "Input: ilimitado",
                        "Output: ilimitado")),
        ENTERPRISE("Enterprise",
                Arrays.asList("Dispositivos ilimitados",
                        "Usuário ilimitados",
                        "Aplicações ilimitadas",
                        "Sob consulta"));

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
