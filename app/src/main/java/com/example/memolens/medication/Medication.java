package com.example.memolens.medication;

import com.google.firebase.Timestamp;

import java.util.*;

public class Medication {
    String dosage, instructions, name;
    Timestamp lastTaken;
    long interval;
    public Medication(Map<String, Object> map) {
        dosage = (String) map.get("dosage");
        instructions = (String) map.get("instructions");
        name = (String) map.get("name");
        interval = (Long) map.get("interval");
        lastTaken = (Timestamp) map.get("last-taken");
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("dosage", dosage);
        map.put("instructions", instructions);
        map.put("name", name);
        map.put("interval", interval);
        map.put("last-taken", lastTaken);
        return map;
    }
}
