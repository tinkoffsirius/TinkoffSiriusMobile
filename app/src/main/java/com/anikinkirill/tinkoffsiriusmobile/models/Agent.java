package com.anikinkirill.tinkoffsiriusmobile.models;

import java.util.List;

/**
 * CREATED BY ANIKINKIRILL
 */
public class Agent {

    private AgentID agent;
    private List<Activity> activities;

    public Agent(AgentID agent, List<Activity> activities) {
        this.agent = agent;
        this.activities = activities;
    }

    public AgentID getAgent() {
        return agent;
    }

    public void setAgent(AgentID agent) {
        this.agent = agent;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
