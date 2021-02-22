package com.konkerlabs.platform.registry.integration.endpoints;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.core.services.JedisTaskService;
import com.konkerlabs.platform.registry.integration.serializers.EventVO;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

public abstract class AbstractEventRestEndpoint {

    protected ApplicationContext applicationContext;
    protected DeviceEventService deviceEventService;
    protected JedisTaskService jedisTaskService;
    protected Executor executor;

    public AbstractEventRestEndpoint(ApplicationContext applicationContext,
                                     DeviceEventService deviceEventService,
                                     JedisTaskService jedisTaskService,
                                     Executor executor) {
        this.applicationContext = applicationContext;
        this.deviceEventService = deviceEventService;
        this.jedisTaskService = jedisTaskService;
        this.executor = executor;
    }

    protected DeferredResult<List<EventVO>> subscribeForEvents(HttpServletResponse httpResponse,
                                      DeferredResult<List<EventVO>> deferredResult,
                                      Device device,
                                      String channel,
                                      Optional<Long> waitTime,
                                      Optional<Long> offset,
                                      Locale locale,
                                      Boolean syncMode) {
        if (waitTime.isPresent() && waitTime.get().compareTo(new Long("30000")) > 0) {
            deferredResult.setErrorResult(applicationContext.getMessage(DeviceEventRestEndpoint.Messages.INVALID_WAITTIME.getCode(), null, locale));
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return deferredResult;
        }

        if(Optional.ofNullable(channel).isPresent() &&
                (channel.length() > 36 || Pattern.compile("[^A-Za-z0-9_-]").matcher(channel).find())){
            deferredResult.setErrorResult(applicationContext.getMessage(DeviceEventRestEndpoint.Messages.INVALID_CHANNEL_PATTERN.getCode(), null, locale));
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return deferredResult;
        }

        Instant startTimestamp = offset.isPresent() ? Instant.ofEpochMilli(offset.get()) : null;
        boolean asc = offset.isPresent();
        Integer limit = offset.isPresent() ? 50 : 1;

        ServiceResponse<List<Event>> response = deviceEventService.findOutgoingBy(device.getTenant(), device.getApplication(), device.getGuid(),
                null, channel, startTimestamp, null, asc, limit);

        if (syncMode &&
                (!response.getResult().isEmpty()
                        || !waitTime.isPresent()
                        || (waitTime.isPresent() && waitTime.get().equals(new Long("0"))))) {
            deferredResult.setResult(EventVO.from(response.getResult()));

        } else if (!response.getResult().isEmpty()
                || !waitTime.isPresent()
                || (waitTime.isPresent() && waitTime.get().equals(new Long("0")))) {
            deferredResult.setResult(EventVO.from(response.getResult()));
        } else {
            CompletableFuture.supplyAsync(() -> {
                String subChannel = Optional.ofNullable(channel).isPresent()
                        ? device.getApiKey()+ '.' +channel
                        : device.getApiKey();
                return jedisTaskService.subscribeToChannel(subChannel, startTimestamp, asc, limit);
            }, executor)
                    .whenCompleteAsync((result, throwable) -> deferredResult.setResult(EventVO.from(result)), executor);
        }

        return deferredResult;
    }

}
