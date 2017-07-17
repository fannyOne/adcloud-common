package com.asiainfo.comm.externalservice.jenkins.jobconfig.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @version v 1.0 on 2016/7/14 9:52
 * @auther william.xu
 */
@XStreamAlias("net.hurstfrost.hudson.sounds.HudsonSoundsNotifier")
public class HudsonSoundsNotifier extends Notifier {

    @XStreamAsAttribute
    private String plugin = "sounds@0.4.3";

    private List<SoundEvent> soundEvents = new ArrayList<>();

    public List<SoundEvent> getSoundEvents() {
        return soundEvents;
    }

    public void setSoundEvents(List<SoundEvent> soundEvents) {
        this.soundEvents = soundEvents;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    @XStreamAlias("net.hurstfrost.hudson.sounds.HudsonSoundsNotifier$SoundEvent")
    public static class SoundEvent {
        private String soundId;

        private Result toResult;

        private Set<Result> fromResults;


        public String getSoundId() {
            return soundId;
        }

        public void setSoundId(String soundId) {
            this.soundId = soundId;
        }

        public Result getToResult() {
            return toResult;
        }

        public void setToResult(Result toResult) {
            this.toResult = toResult;
        }

        public Set<Result> getFromResults() {
            return fromResults;
        }

        public void setFromResults(Set<Result> fromResults) {
            this.fromResults = fromResults;
        }
    }
}
