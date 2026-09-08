package lt.vaeloria.ooc;

import java.util.*;

/** Undirected roads; traversal cannot jump across unexplored intermediate locations. */
final class WorldRoutes {
    static final String[][] EDGES={
        {"Luminara","Veyrhold"},{"Luminara","Asterio Karūna"},{"Luminara","Stiklo Giria"},{"Luminara","Aureliono Pakraštys"},
        {"Veyrhold","Kharad Vorn"},{"Stiklo Giria","Amžinojo Šaltinio Slėnis"},{"Amžinojo Šaltinio Slėnis","Šventųjų Pelkynas"},
        {"Šventųjų Pelkynas","Žaliasis Labirintas"},{"Žaliasis Labirintas","Tuščiavidurė Smailė"},
        {"Tuščiavidurė Smailė","Žvaigždėkritos Skliautas"},{"Žvaigždėkritos Skliautas","Aureliono Pakraštys"},
        {"Aureliono Pakraštys","Pelenų Karūnos Citadelė"},{"Aureliono Pakraštys","Drakono Pabudimo Viršūnės"},
        {"Aureliono Pakraštys","Safyro Platybės"},{"Safyro Platybės","Žaliasis Labirintas"}
    };
    static Set<String> neighbors(String location){
        LinkedHashSet<String> names=new LinkedHashSet<>();
        for(String[] edge:EDGES){if(edge[0].equals(location))names.add(edge[1]);if(edge[1].equals(location))names.add(edge[0]);}
        return names;
    }
    static List<String> path(String from,String to,Set<String> discovered){
        if(from.equals(to)||!discovered.contains(to))return Collections.emptyList();
        ArrayDeque<String> queue=new ArrayDeque<>();Map<String,String> previous=new HashMap<>();queue.add(from);previous.put(from,null);
        while(!queue.isEmpty()){
            String at=queue.remove();if(at.equals(to))break;
            for(String next:neighbors(at))if(discovered.contains(next)&&!previous.containsKey(next)){previous.put(next,at);queue.add(next);}
        }
        if(!previous.containsKey(to))return Collections.emptyList();
        LinkedList<String> result=new LinkedList<>();for(String at=to;at!=null;at=previous.get(at))result.addFirst(at);return result;
    }
    private WorldRoutes(){}
}
