package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LocationTreeUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(LocationTreeUtils.class);

    public static Location buildTree(List<Location> allNodes) {

        Location root = null;

        Map<String, List<Location>> childrenListMap = new HashMap<>();

        for (Location location: allNodes) {
            if (location.getParent() == null) {
                root = location;
            } else {
                String parentGuid = location.getParent().getGuid();
                List<Location> children = childrenListMap.computeIfAbsent(parentGuid, k -> new ArrayList<>());
                children.add(location);
             }
        }

        for (Location location: allNodes) {
            List<Location> children = childrenListMap.get(location.getGuid());
            if (children == null) {
                children = new ArrayList<>();
            }
            location.setChildren(children);
        }

        return root;
    }

    public static List<Location> getNodesList(Location root) {
        if (root == null) {
            return null;
        }

        List<Location> nodes = new ArrayList<>();
        nodes.add(root);

        if (root.getChildren() != null) {
            for (Location child: root.getChildren()) {
                List<Location> childNodes = getNodesList(child);
                if (childNodes != null && !childNodes.isEmpty()) {
                    nodes.addAll(childNodes);
                }
            }
        }

        return nodes;
    }

    public static List<Location> getNodesListBreadthFirstOrder(Location root) {
        if (root == null) {
            return null;
        }

        List<Location> nodes = new ArrayList<>();

        Queue<Location> queue = new LinkedList<>() ;
        queue.add(root);

        while(!queue.isEmpty()) {
            Location node = queue.remove();
            nodes.add(node);

            if (node.getChildren() != null) {
                queue.addAll(node.getChildren());
            }
        }

        return nodes;
    }

    public static Location searchLocationByName(Location root, String locationName, int deep) {

        if (deep > 50) {
            LOGGER.warn("Too deep structure. Cyclic graph?");
            return null;
        }

        if (root == null || root.getName().equals(locationName)) {
            return root;
        }

        for (Location child : root.getChildren()) {
            Location element = searchLocationByName(child, locationName, deep + 1);
            if (element != null) {
                return element;
            }
        }

        return null;

    }

    public static List<Location> listNewLocations(Location currentTree, Location newTree) {

        List<Location> newNodes = new ArrayList<>();

        List<Location> newTreeNodes = getNodesListBreadthFirstOrder(newTree);
        Set<String> currentNodesSet = getNodesSetName(getNodesList(currentTree));

        for (Location location : newTreeNodes) {
            if (!currentNodesSet.contains(location.getName())) {
                newNodes.add(location);
            }
        }

        // deepest location first
        Collections.reverse(newNodes);

        return newNodes;

    }

    public static List<Location> listRemovedLocations(Location currentTree, Location newTree) {

        List<Location> removedNodes = new ArrayList<>();

        List<Location> currentNodes = getNodesListBreadthFirstOrder(currentTree);
        Set<String> newNodesSet = getNodesSetName(getNodesList(newTree));

        for (Location location : currentNodes) {
            if (!newNodesSet.contains(location.getName())) {
                removedNodes.add(location);
            }
        }

        // deepest location first
        Collections.reverse(removedNodes);

        return removedNodes;

    }

        public static List<Location> listExistingLocations(Location currentTree, Location newTree) {

        List<Location> existingLocations = new ArrayList<>();

        Set<String> currentNodesMap = getNodesSetName(getNodesList(currentTree));
        List<Location> newNodes = getNodesList(newTree);

        for (Location location : newNodes) {
            if (currentNodesMap.contains(location.getName())) {
                existingLocations.add(location);
            }
        }

        return existingLocations;
    }

    private static Set<String> getNodesSetName(List<Location> currentNodes) {

        Set<String> locationsSet = new HashSet<>();

        for (Location location : currentNodes) {
            locationsSet.add(location.getName());
        }

        return locationsSet;

    }

    public static Boolean isSublocationOf(Location root, Location childCandidate) {
        return getNodesListBreadthFirstOrder(root)
                .stream().anyMatch(item -> item != null && childCandidate != null
                        && childCandidate.getName() != null &&
                        childCandidate.getName().equals(item.getName()));
    }
}
