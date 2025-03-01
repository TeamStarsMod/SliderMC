package xyz.article.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.event.Event;
import xyz.article.api.event.EventExecutor;
import xyz.article.api.event.EventManager;
import xyz.article.api.event.Listener;
import xyz.article.api.event.events.ClientPingEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class EventManagerInstant implements EventManager {
    private static final Logger log = LoggerFactory.getLogger(EventManagerInstant.class);
    private final List<Listener> listeners;
    public EventManagerInstant () {
        listeners = new ArrayList<>();
    }

    @Override
    public Event callEvent (Event event) {
        for (Listener listener : listeners) {
            try {
                Class<?> clazz = listener.getClass();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    //获取被注解的方法
                    if (method.isAnnotationPresent(EventExecutor.class)) {
                        Parameter[] parameters = method.getParameters();
                        if (parameters.length > 0) {
                            //获取实参
                            Class<?> firstParameterClass = parameters[0].getType();
                            //检查参数并执行
                            if (firstParameterClass.isInstance(event)) method.invoke(listener, event);
                        } else {
                            throw new IllegalArgumentException("事件监听器 " + listener.getClass().getName() + " 的编写格式异常！");
                        }
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("在执行监听器 {} 的时候发生异常 {}", listener.getClass().getName(), e);
            }
        }
        return event;
    }

    @Override
    public void addListener (Listener listener) {
        if (listeners.contains(listener)) throw new IllegalArgumentException("这个监听器已经被注册了！监听器实例 " + listener);
        else listeners.add(listener);
    }

    @Override
    public void removeListener (Listener listener) {
        if (listeners.contains(listener)) listeners.remove(listener);
        else throw new IllegalArgumentException("这个监听器还没有被注册，无法移除！监听器实例 " + listener);
    }

    public static class ExampleListener implements Listener {
        @EventExecutor
        public void onEvent (ClientPingEvent event) {
        }
    }
}
