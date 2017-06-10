package com.hosec.homesecurity.remote;

import com.hosec.homesecurity.model.Device;
import com.hosec.homesecurity.model.Notification;
import com.hosec.homesecurity.model.Rule;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D062572 on 29.05.2017.
 */

public class RemoteAlarmSystem {

    private static List<Device> deviceList;
    private static List<Notification> notificationList;
    private static List<Rule> ruleList;

    static {
        deviceList = new ArrayList<>();
        notificationList = new ArrayList<>();
        ruleList = new ArrayList<>();

        long id = 0;
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.DISCONNECTED, Device.Type.SENSOR, Device.InterfaceType.UART, null));
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.CONNECTED, Device.Type.ACTOR, Device.InterfaceType.UART, null));
        try {
            deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.OFFLINE, Device.Type.ACTOR, Device.InterfaceType.IP, new URL("http://192.168.2.1:30/bullshit")));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.CONNECTED, Device.Type.SENSOR, Device.InterfaceType.UART, null));
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.DISCONNECTED, Device.Type.ACTOR, Device.InterfaceType.UART, null));
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.CONNECTED, Device.Type.SENSOR, Device.InterfaceType.UART, null));
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.CONNECTED, Device.Type.ACTOR, Device.InterfaceType.UART, null));
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.OFFLINE, Device.Type.SENSOR, Device.InterfaceType.UART, null));
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.DISCONNECTED, Device.Type.SENSOR, Device.InterfaceType.UART, null));
        deviceList.add(new Device(id++, "Device" + id, "o.A.", Device.State.DISCONNECTED, Device.Type.SENSOR, Device.InterfaceType.UART, null));

        ruleList.add(new Rule(false));
        ArrayList<Device> dummySensors = new ArrayList<Device>();
        ArrayList<Device> dummyActors = new ArrayList<Device>();
        dummyActors.add(deviceList.get(1));
        dummySensors.add(deviceList.get(3));
        dummySensors.add(deviceList.get(3));
        dummySensors.add(deviceList.get(3));
        dummySensors.add(deviceList.get(3));
        dummySensors.add(deviceList.get(3));
        dummySensors.add(deviceList.get(3));


        ruleList.add(new Rule(1, true, dummySensors, dummyActors, "Rule 1"));
        ruleList.add(new Rule(2, true, dummySensors, dummyActors, "Rule 2"));

    }


    public static List<Notification> getAllNotifications() {
        return new ArrayList<>(notificationList);
    }

    public static List<Device> getAllDevices() {
        return new ArrayList<>(deviceList);
    }

    public static List<Rule> getAllRules() {
        return new ArrayList<>(ruleList);
    }

    public static void updateDeviceInformation(Device device) {
        for (Device d : deviceList) {
            if (d.getID() == device.getID()) {
                d.setState(device.getState());
                d.setUrl(device.getUrl());
                d.setName(device.getName());
                break;
            }
        }
    }

    public static void updateRuleInformation(Rule rule) {
        for (Rule r : ruleList) {
            if (r.getID() == rule.getID()) {
                ruleList.remove(r);
                ruleList.add(rule);
                break;
            }
        }
    }

    public static void addNewRule(Rule rule) {
        rule.setID(ruleList.get(ruleList.size() - 1).getID() + 1);
        ruleList.add(rule);
    }

    public static void changePassword(String newPassword){

    }

    public static void changeUsername(String newUsername){

    }

    public static boolean checkSystem(String hostname, String username, String password){
        return hostname.equals("hosec.com")&& username.equals("test") && password.equals("1234");
    }


}
