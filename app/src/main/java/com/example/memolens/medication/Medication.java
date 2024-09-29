package com.example.memolens.medication;

import com.google.firebase.Timestamp;

import java.util.*;

public class Medication {
    public String dosage, instructions, name;
    public Timestamp lastTaken;
    public long interval;
    public int id = -1;
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

    public void createId(Set<Integer> set) {
        Random random = new Random();
        do {
            id = random.nextInt(Integer.MAX_VALUE);
        } while (set.contains(id));

        set.add(id);
    }

    public static Medication createMedication(String n, String d, String i,
                                       long inter, Timestamp last) {
        Map<String, Object> map = new HashMap<>();
        map.put("dosage", d);
        map.put("instructions", i);
        map.put("name", n);
        map.put("interval", inter);
        map.put("last-taken", last);
        return new Medication(map);
    }
}
