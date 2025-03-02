package xyz.article.api.entities;

import xyz.article.RunningData;

import java.util.Random;

public class EntityID {
    /**
     * 用于获取不重复的随机EntityId
     * @return EntityId
     */
    public static int getRandomEntityId() {
        int id = new Random().nextInt();
        while (true) {
            if (RunningData.globalEntities.contains(id)) {
                id = new Random().nextInt();
            } else {
                break;
            }
        }
        return id;
    }
}
