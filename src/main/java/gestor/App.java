package gestor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;

import io.github.cdimascio.dotenv.Dotenv;

class Equipo{
    public String nombre;
}

class Partido{
    public int id_partido;
    public String equipo1;
    public String equipo2;
    public String equipo_ganador;
    public int equipo1_goles;
    public int equipo2_goles;
}

class Jugador{
    public int dni;
    public String nombre;
    public String nombre_equipo;
}

public class App{
    static boolean isNumeric(String str){
        for(int i =0; i < str.length(); i++){
            if(str.charAt(i) < '0' || str.charAt(i) > '9')
                return false;
        }
        return true;
    }

    static  ArrayList<Jugador> obtenerListaJugadores(Connection conn, String equipo){
        ArrayList<Jugador> jugadores = new ArrayList<>();
        try{
            Statement st = conn.createStatement();
            ResultSet jugadoresQuery = st.executeQuery("SELECT dni, nombre, nombre_equipo FROM jugador WHERE nombre_equipo = '" + equipo + "'");
            while(jugadoresQuery.next()){
                Jugador jugador = new Jugador();
                jugador.nombre = jugadoresQuery.getString("nombre");
                jugador.dni = jugadoresQuery.getInt("dni");
                jugador.nombre_equipo = jugadoresQuery.getString("nombre_equipo");
                jugadores.add(jugador);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jugadores;
    }
    static ArrayList<Jugador> obtenerListaJugadores(Connection conn){
        ArrayList<Jugador> Jugadores = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet jugadoresQuery = st.executeQuery("SELECT dni, nombre, nombre_equipo FROM jugador WHERE dni IS NOT NULL");
            while(jugadoresQuery.next()){
                Jugador jugador = new Jugador();
                jugador.dni = jugadoresQuery.getInt("dni");
                jugador.nombre = jugadoresQuery.getString("nombre");
                jugador.nombre_equipo = jugadoresQuery.getString("nombre_equipo");
                Jugadores.add(jugador);
            }
            jugadoresQuery.close();
        }catch(Exception e){
            System.out.println("ERROR: No se pudo obtener lista de jugadores.");
            throw new RuntimeException(e);
        }
        return Jugadores;
    };

    static ArrayList<Equipo> obtenerListaEquipos(Connection conn){
        ArrayList<Equipo> Equipos = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet equiposQuery = st.executeQuery("SELECT nombre FROM equipo WHERE nombre IS NOT NULL");
            while(equiposQuery.next()){
                Equipo equipo = new Equipo();
                equipo.nombre = equiposQuery.getString("nombre");
                Equipos.add(equipo);
            }
            equiposQuery.close();
        }catch(Exception e){
            System.out.println("ERROR: No se pudo obtener lista de equipos.");
        }
        return Equipos;
    }

    static void nombrarJugadores(ArrayList<Jugador> Jugadores){
        int Cantidad = 0;
        for(Jugador jugador : Jugadores){
            Cantidad += 1;
            String equipoNombre = jugador.nombre_equipo;
            if(jugador.nombre_equipo == null){
                equipoNombre = "Sin equipo";
            }
            System.out.println(Cantidad+". Nombre: "+jugador.nombre+" | DNI: "+jugador.dni+" | Equipo: "+equipoNombre);
        }
    }

    static void nombrarEquipos(ArrayList<Jugador> Jugadores, ArrayList<Equipo> Equipos){
        int Cantidad = 0;
        int jugadoresEnEquipo = 0;

        for(Equipo equipo : Equipos){
            Cantidad += 1;
            for(Jugador jugador : Jugadores){
                if(jugador.nombre_equipo.equals(equipo.nombre)){
                    jugadoresEnEquipo += 1;
                }
            }
            System.out.println(Cantidad+". Equipo: "+equipo.nombre+" | Cantidad de jugadores: "+jugadoresEnEquipo);
            jugadoresEnEquipo = 0;
        }
    }

    static  void nombrarJugadoresDeEquipo(ArrayList<Jugador> Jugadores, String nombre){
        int Cantidad = 0;
        for(Jugador jugador : Jugadores){
            if(jugador.nombre_equipo.equals(nombre)){
                Cantidad += 1;
                System.out.println(Cantidad+". Nombre: "+jugador.nombre+" | DNI: "+jugador.dni);
            }
        }
    }

    static void revistarListaJugadores(Connection conn, Scanner input){
        try{
            Statement st = conn.createStatement();
            String option;
            do{
                System.out.println("1. Mostrar todos los jugadores y sus equipos.");
                System.out.println("2. Crear jugador.");
                System.out.println("3. Borrar jugador.");
                System.out.println("0. Volver hacia atras.");
                option = input.next();
                switch (option){
                    case "1":{
                        ArrayList<Jugador> Jugadores = obtenerListaJugadores(conn);
                        System.out.println("Los jugadores y sus equipos son:");
                        nombrarJugadores(Jugadores);
                        break;
                    }

                    case "2":{
                        ArrayList<Jugador> Jugadores = obtenerListaJugadores(conn);
                        ArrayList<Equipo> Equipos = obtenerListaEquipos(conn);

                        System.out.println("Ingrese el nombre del nuevo jugador:");
                        String Nombre = input.next();
                        System.out.println("Ingrese el DNI del nuevo jugador:");
                        String DNItemp = input.next();
                        int DNI;
                        try{
                            DNI = Integer.parseInt(DNItemp);
                            if(DNI < 0){
                                System.out.println("DNI invalido.");
                                break;
                            };
                            boolean dniExistente = false;
                            for(Jugador jugador : Jugadores){
                                if(jugador.dni == DNI){
                                    System.out.println("Un jugador ya existe con ese DNI.");
                                    dniExistente = true;
                                    break;
                                }
                            }
                            if(dniExistente){
                                break;
                            }
                        } catch (NumberFormatException e){
                            System.out.println("DNI invalido.");
                            break;
                        };

                        if(Equipos.isEmpty()) {
                            System.out.println("No existen ningun equipo al cual se pueda asignar el jugador.");
                            break;
                        }

                        System.out.println("Ingrese el equipo existente al cual va a pertenecer el nuevo jugador, Ingrese 'VER' si quiere ver todos los equipos registrados primero.");
                        String equipoJugador = input.next();
                        if(equipoJugador.equals("VER")){
                            System.out.println("Los equipos registrados son:");
                            nombrarEquipos(Jugadores,Equipos);
                            System.out.println("Ahora ingrese el NOMBRE del equipo registrado al cual el jugador va a pertenecer.");
                            equipoJugador = input.next();
                        }

                        boolean Existe = false;

                        for(Equipo equipo : Equipos){
                           if(equipo.nombre.equals(equipoJugador)){
                               Existe = true;
                               break;
                           }
                        }

                        if(!Existe){
                            System.out.println("Ese equipo no esta registrado.");
                            break;
                        }

                        System.out.println("El jugador "+Nombre+" ha sido registrado.");
                        if(equipoJugador == null){
                            st.executeUpdate(String.format("INSERT INTO jugador(nombre, dni) VALUES('%s', %d)", Nombre, DNI));
                        }else{
                            st.executeUpdate(String.format("INSERT INTO jugador(nombre, dni, nombre_equipo) VALUES('%s', %d, '%s')", Nombre, DNI, equipoJugador));
                        }
                        break;
                    }

                    case "3":{
                        ArrayList<Jugador> Jugadores = obtenerListaJugadores(conn);
                        System.out.println("Ingrese el DNI del jugador a borrar:");
                        String DNItemp = input.next();
                        int DNI;
                        try{
                            DNI = Integer.parseInt(DNItemp);
                        } catch (NumberFormatException e){
                            System.out.println("DNI invalido.");
                            break;
                        };

                        Jugador jugadorAfectado = null;

                        for(Jugador jugador : Jugadores){
                            if(jugador.dni == DNI){
                                jugadorAfectado = jugador;
                                break;
                            }
                        }

                        if(jugadorAfectado == null){
                            System.out.println("No existe un jugador con ese DNI en el registro.");
                            break;
                        }

                        System.out.println("El jugador "+jugadorAfectado.nombre+" fue eliminado del registro.");
                        st.executeUpdate("DELETE FROM jugador_partido WHERE dni_jugador = " + DNI);
                        st.executeUpdate("DELETE FROM jugador WHERE dni = " + DNI);
                        /* TODO: hacer QUERY para eliminar el JUGADOR de la BD.
                               La variable a utilizar es "DNI".
                        */

                        break;
                    }

                    case "0":{
                        break;
                    }

                    default:{
                        System.out.println("Opcion no valida.");
                    }
                }

            }while(!option.equals("0"));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    static void revistarListaEquipos(Connection conn, Scanner input){
        try{
            Statement st = conn.createStatement();
            String option;
            do {
                System.out.println("1. Mostrar todos los equipos y sus jugadores.");
                System.out.println("2. Crear un equipo.");
                System.out.println("3. Añadir un jugador sin equipo a un equipo.");
                System.out.println("4. Quitar un jugador de un equipo.");
                System.out.println("5. Borrar un equipo.");
                System.out.println("0. Volver hacia atras.");
                option = input.next();
                switch (option){
                    case "1":{
                        ArrayList<Jugador> Jugadores = obtenerListaJugadores(conn);
                        ArrayList<Equipo> Equipos = obtenerListaEquipos(conn);

                        if(Equipos.isEmpty()){
                            System.out.println("Hay 0 equipos registrados.");
                            break;
                        }

                        System.out.println("Los equipos registrados son:");
                        nombrarEquipos(Jugadores,Equipos);

                        System.out.println("Ingrese el NOMBRE del equipo que quiera ver sus jugadores, o cualquier otra cosa para salir.");

                        String nombre = input.next();
                        boolean Existe = false;

                        for(Equipo equipo : Equipos){
                            if(equipo.nombre.equals(nombre)){
                                Existe = true;
                                break;
                            }
                        }

                        if(!Existe){
                            break;
                        }

                        System.out.println("Los jugadores del equipo "+nombre+" son:");
                        nombrarJugadoresDeEquipo(Jugadores,nombre);

                        break;
                    }

                    case "2":{
                        System.out.println("Ingrese el nombre del nuevo equipo:");
                        String Nombre = input.next();

                        ArrayList<Equipo> Equipos = obtenerListaEquipos(conn);

                        for(Equipo equipo : Equipos){
                            if(equipo.nombre.equals(Nombre)){
                                System.out.println("Ya existe un equipo con ese nombre.");
                                break;
                            }
                        }

                        System.out.println("El equipo "+Nombre+" ha sido registrado.");
                        st.executeUpdate("INSERT INTO equipo(nombre) VALUES('" + Nombre + "')");

                        break;
                    }

                    case "3":{
                        ArrayList<Jugador> Jugadores = obtenerListaJugadores(conn);
                        ArrayList<Equipo> Equipos = obtenerListaEquipos(conn);
                        ArrayList<Jugador> jugadoresSolos = new ArrayList<>();

                        for(Jugador jugador : Jugadores){
                            if(jugador.nombre_equipo == null){
                                jugadoresSolos.add(jugador);
                            }
                        }

                        if(jugadoresSolos.isEmpty()){
                            System.out.println("No hay jugadores sin equipo registrados.");
                            break;
                        }

                        System.out.println("Los jugadores sin equipo son:");
                        int Cantidad = 0;
                        for(Jugador jugador : jugadoresSolos){
                            Cantidad += 1;
                            System.out.println(Cantidad+". Nombre: "+jugador.nombre+" DNI: "+jugador.dni);
                        }

                        System.out.println("Ingrese el DNI del jugador que quiera añadir a un equipo.");
                        String DNItemp = input.next();
                        int DNI;
                        try{
                            DNI = Integer.parseInt(DNItemp);
                        } catch (NumberFormatException e){
                            System.out.println("DNI invalido.");
                            break;
                        };

                        Jugador jugadorSinEquipo = null;

                        for(Jugador jugador : jugadoresSolos){
                            if(jugador.dni == DNI){
                                jugadorSinEquipo = jugador;
                            }
                        }

                        if(jugadorSinEquipo == null){
                            System.out.println("Ese jugador no se encuentra en el registro.");
                            break;
                        }

                        System.out.println("Ingrese el equipo existente al cual va a pertenecer el jugador, Ingrese 'VER' si quiere ver todos los equipos registrados primero.");
                        String equipoJugador = input.next();
                        if(equipoJugador.equals("VER")){
                            System.out.println("Los equipos registrados son:");
                            nombrarEquipos(Jugadores,Equipos);
                            System.out.println("Ahora ingrese el NOMBRE del equipo registrado al cual el jugador va a pertenecer.");
                            equipoJugador = input.next();
                        }

                        Equipo equipoIntegrador = null;

                        for(Equipo equipo : Equipos){
                            if(equipo.nombre.equals(equipoJugador)){
                                equipoIntegrador = equipo;
                            }
                        }

                        if(equipoIntegrador == null){
                            System.out.println("Ese equipo no esta registrado.");
                            break;
                        }

                        System.out.println("El jugador "+jugadorSinEquipo.nombre+" ha sido integrado a "+equipoIntegrador.nombre+".");

                        st.executeUpdate("UPDATE jugador SET nombre_equipo = '" + equipoJugador + "' WHERE dni = " + DNI);
                        /* TODO: hacer QUERY para cambiar los datos del JUGADOR en la BD.
                               El registro del JUGADOR puede ser obtenido con la variable "DNI".
                               El campo a cambiar es "equipo", el cual debe ser cambiado a equipoJugador.
                        */
                        break;
                    }

                    case "4":{
                        ArrayList<Jugador> Jugadores = obtenerListaJugadores(conn);
                        ArrayList<Equipo> Equipos = obtenerListaEquipos(conn);
                        ArrayList<Jugador> jugadoresEnEquipo = new ArrayList<>();

                        System.out.println("Ingrese el equipo existente al cual va a quitar un jugador, Ingrese 'VER' si quiere ver todos los equipos registrados primero.");
                        String equipoJugador = input.next();
                        if(equipoJugador.equals("VER")){
                            System.out.println("Los equipos registrados son:");
                            nombrarEquipos(Jugadores,Equipos);
                            System.out.println("Ahora ingrese el NOMBRE del equipo registrado del cual se va a borrar un jugador.");
                            equipoJugador = input.next();
                        }

                        Equipo equipoAfectado = null;

                        for(Equipo equipo : Equipos){
                            if(equipo.nombre.equals(equipoJugador)){
                                equipoAfectado = equipo;
                                break;
                            }
                        }

                        if(equipoAfectado == null){
                            System.out.println("Ese equipo no se encuentra en el registro.");
                            break;
                        }

                        for(Jugador jugador : Jugadores){
                            if(jugador.nombre_equipo.equals(equipoAfectado.nombre)){
                                jugadoresEnEquipo.add(jugador);
                            }
                        }

                        if(jugadoresEnEquipo.isEmpty()){
                            System.out.println("Ese equipo no posee ningun jugador para remover.");
                            break;
                        }

                        System.out.println("Ingrese el DNI del jugador que desee remover del equipo.");

                        String DNItemp = input.next();
                        int DNI;
                        try{
                            DNI = Integer.parseInt(DNItemp);
                        } catch (NumberFormatException e){
                            System.out.println("DNI invalido.");
                            break;
                        };

                        Jugador jugadorAfectado = null;
                        for(Jugador jugador : jugadoresEnEquipo){
                            if(jugador.dni == DNI){
                                jugadorAfectado = jugador;
                            }
                        }

                        if(jugadorAfectado == null){
                            System.out.println("No existe un jugador con ese DNI en el registro de este equipo.");
                            break;
                        }

                        System.out.println("El jugador "+jugadorAfectado.nombre+" ha sido removido del equipo "+equipoAfectado.nombre+".");

                        st.executeUpdate("UPDATE jugador SET nombre_equipo = NULL WHERE dni = " + DNI);
                        /* TODO: hacer QUERY para cambiar los datos del JUGADOR en la BD.
                               El registro del JUGADOR puede ser obtenido con la variable "DNI".
                               El campo a cambiar es "equipo", el cual debe ser asignado a null.
                        */
                        break;
                    }

                    case "5":{
                        ArrayList<Jugador> Jugadores = obtenerListaJugadores(conn);
                        ArrayList<Equipo> Equipos = obtenerListaEquipos(conn);
                        ArrayList<Jugador> jugadoresEnEquipo = new ArrayList<>();

                        System.out.println("Ingrese el equipo existente al cual va a eliminar, Ingrese 'VER' si quiere ver todos los equipos registrados primero.");
                        String equipoJugador = input.next();
                        if(equipoJugador.equals("VER")){
                            System.out.println("Los equipos registrados son:");
                            nombrarEquipos(Jugadores,Equipos);
                            System.out.println("Ahora ingrese el NOMBRE del equipo registrado a eliminar.");
                            equipoJugador = input.next();
                        }

                        Equipo equipoAfectado = null;

                        for(Equipo equipo : Equipos){
                            if(equipo.nombre.equals(equipoJugador)){
                                equipoAfectado = equipo;
                                break;
                            }
                        }

                        if(equipoAfectado == null){
                            System.out.println("Ese equipo no se encuentra en el registro.");
                            break;
                        }
                        ResultSet existePartido = st.executeQuery("SELECT COUNT(*) FROM partido WHERE equipo1 = '" + equipoAfectado.nombre + "' OR equipo2 = '" + equipoAfectado.nombre + "'");
                        existePartido.next();
                        if(existePartido.getInt(1) != 0){
                            System.out.println("Equipo no se puede borrar porque ya jugo un partido. Borre los partidos para borrar el equipo");
                            break;
                        }
                        

                        for(Jugador jugador : Jugadores){
                            if(jugador.nombre_equipo.equals(equipoAfectado.nombre)){
                                jugadoresEnEquipo.add(jugador);
                            }
                        }

                        System.out.println("El equipo "+equipoAfectado.nombre+"posee "+jugadoresEnEquipo.size()+" jugador(es).");

                        if(jugadoresEnEquipo.isEmpty()){
                            System.out.println("El equipo fue eliminado del registro.");
                            st.executeUpdate("DELETE FROM equipo WHERE equipo = '" + equipoAfectado.nombre + "'");
                            /*TODO: hacer QUERY para eliminar el EQUIPO de la BD.
                               El campo a utilizar es equipoJugador.
                            */
                            break;
                        }

                        System.out.println("Ingrese 0 si quiere también eliminar a todos los jugadores del equipo, o cualquier otra cosa si quiere que los jugadores solo sean asignados sin equipo.");
                        String opcion = input.next();
                        if(opcion.equals("0")){
                            /*TODO: hacer QUERY para eliminar el EQUIPO de la BD.
                               El campo a utilizar es equipoJugador.
                            */
                            /*TODO: hacer QUERY para cambiar los datos de los JUGADOR(es) en la BD.
                               Todos los Jugador en "jugadoresEnEquipo" deben ser eliminados de la BD.
                            */
                            st.executeUpdate("DELETE FROM jugador WHERE nombre_equipo = '" + equipoAfectado.nombre + "'");
                            st.executeUpdate("DELETE FROM equipo WHERE nombre = '" + equipoAfectado.nombre + "'");
                            System.out.println("El equipo "+equipoAfectado.nombre+" y sus jugadores han sido eliminados.");
                            break;
                        }

                        System.out.println("El equipo "+equipoAfectado.nombre+" ha sido eliminado del registro.");
                        /*TODO: hacer QUERY para eliminar el EQUIPO de la BD.
                            El campo a utilizar es equipoJugador.
                        */

                        /*TODO: hacer QUERY para cambiar los datos de los JUGADOR(es) en la BD.
                           Todos los Jugador en "jugadoresEnEquipo" deben tener su campo "equipo" cambiado a null.
                        */

                        st.executeUpdate("UPDATE jugador SET nombre_equipo = NULL WHERE nombre_equipo = '" + equipoAfectado.nombre + "'");
                        st.executeUpdate("DELETE FROM equipo WHERE nombre = '" + equipoAfectado.nombre + "'");
                        break;
                    }

                    case "0":{
                        break;
                    }

                    default:{
                        System.out.println("Opcion no valida.");
                    }
                }
            }while(!option.equals("0"));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    static void editarPartido(Connection conn, Scanner input, Partido partido){
        while(true){
            System.out.println("Ingrese los goles de " + partido.equipo1);
            String option = input.next();
            if(!isNumeric(option)){
                System.out.println("Ingrese un numero");
                continue;
            }
            partido.equipo1_goles = Integer.parseInt(option);
            System.out.println("Ingrese los goles de " + partido.equipo2);
            option = input.next();
            if(!isNumeric(option)){
                System.out.println("Ingrese un numero");
                continue;
            }
            partido.equipo2_goles = Integer.parseInt(option);
            break;
        }
        try{
            Statement st = conn.createStatement();
            st.executeUpdate(String.format("UPDATE partido SET equipo1_goles = %d, equipo2_goles = %d WHERE id_partido = %d", partido.equipo1_goles, partido.equipo2_goles, partido.id_partido));
            st.close();
        }catch (Exception  e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }
    static void borrarPartido(Connection conn, Partido partido){
        try{
            Statement st = conn.createStatement();
            st.executeUpdate(String.format("DELETE FROM partido WHERE id_partido = %d", partido.id_partido));
            st.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    static void mostrarJugadoresPartidos(Connection conn, Partido partido){
        try{
            Statement st = conn.createStatement();
            ResultSet query = st.executeQuery("SELECT nombre, dni_jugador FROM jugador_partido LEFT JOIN jugador ON jugador_partido.dni_jugador = jugador.dni WHERE id_partido = " + partido.id_partido);
            while(query.next()){
                System.out.println("Jugador: " + query.getString("nombre") + " | " + "DNI: " + query.getInt("dni_jugador"));
            }
            st.close();
        }catch (Exception  e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    static void revisarListaPartidos(Connection conn, Scanner input){
        try {
            Statement st = conn.createStatement();
            String option;
            do {
                HashMap<Integer, Partido> allGames = new HashMap<>();
                HashSet<String> equipos = new HashSet<>();
                ResultSet allGamesQuery = st.executeQuery("SELECT id_partido,equipo1, equipo2, equipo1_goles, equipo2_goles FROM partido");
                while(allGamesQuery.next()){
                    Partido partido = new Partido();
                    partido.id_partido = allGamesQuery.getInt("id_partido");
                    partido.equipo1 = allGamesQuery.getString("equipo1");
                    partido.equipo2 = allGamesQuery.getString("equipo2");
                    partido.equipo1_goles = allGamesQuery.getInt("equipo1_goles");
                    partido.equipo2_goles = allGamesQuery.getInt("equipo2_goles");
                    allGames.put(partido.id_partido, partido);
                }
                allGamesQuery.close();
                ResultSet equiposQuery = st.executeQuery("SELECT nombre FROM equipo");
                while(equiposQuery.next()){
                    equipos.add(equiposQuery.getString("nombre"));
                }
                equiposQuery.close();

                System.out.println("1. Mostrar todos los partidos.");
                System.out.println("2. Crear un partido.");
                System.out.println("3. Editar un partido");
                System.out.println("4. Borrar un partido");
                System.out.println("5. Ver detalles del partido");
                System.out.println("0. Volver hacia atras.");
                option = input.next();
                switch (option) {
                    case "1":{
                        for(Partido partido : allGames.values()){
                            System.out.println(String.format("%d. %s %d | %d %s", partido.id_partido, partido.equipo1, partido.equipo1_goles, partido.equipo2_goles, partido.equipo2));
                        }
                        break;
                    }
                    case "2":{

                        for(String equipo : equipos){
                            System.out.println(equipo);
                        }
                        System.out.println("Ingrese el nombre del primer equipo");
                        String equipo1 = input.next();
                        System.out.println("Ingrese el nombre del segundo equipo");
                        String equipo2 = input.next();
                        if(equipo1.equals(equipo2)){
                            System.out.println("ERROR: equipos iguales");
                            break;
                        }
                        if(!equipos.contains(equipo1) || !equipos.contains(equipo2)){
                            System.out.println("ERROR: equipo no existente");
                            break;
                        }
                        ArrayList<Jugador> jugadoresEquipo1 = obtenerListaJugadores(conn, equipo1);
                        ArrayList<Jugador> jugadoresEquipo2 = obtenerListaJugadores(conn, equipo2);

                        st.executeUpdate(String.format("INSERT INTO partido(equipo1, equipo2) VALUES('%s', '%s')", equipo1, equipo2));
                        ResultSet idPartidoQuery = st.executeQuery("SELECT MAX(id_partido) FROM partido");
                        idPartidoQuery.next();
                        int idPartido = idPartidoQuery.getInt(1);
                        idPartidoQuery.close();

                        for(Jugador jugador : jugadoresEquipo1){
                            st.executeUpdate(String.format("INSERT INTO jugador_partido(dni_jugador, id_partido) VALUES(%d, %d)", jugador.dni, idPartido));
                        }
                        for(Jugador jugador : jugadoresEquipo2){
                            st.executeUpdate(String.format("INSERT INTO jugador_partido(dni_jugador, id_partido) VALUES(%d, %d)", jugador.dni, idPartido));
                        }

                        System.out.println("Partido creado con exito");

                        break;
                    }
                    case "3":{
                        for(Partido partido : allGames.values()){
                            System.out.println(String.format("%d. %s %d | %d %s", partido.id_partido, partido.equipo1, partido.equipo1_goles, partido.equipo2_goles, partido.equipo2));
                        }
                        while(true){
                            System.out.println("Ingrese el id del partido a editar");
                            System.out.println("Ingrese (0) para volver");
                            String idPartidoString = input.next();
                            if(idPartidoString.equals("0")){
                                break;
                            }
                            if(!isNumeric(idPartidoString)){
                                System.out.println("Ingrese un numero");
                                continue;
                            }
                            int idPartido = Integer.parseInt(idPartidoString);
                            if(!allGames.containsKey(idPartido)){
                                System.out.println("No existe un partido con ese id");
                                continue;
                            }
                            Partido partido = allGames.get(idPartido);
                            editarPartido(conn, input, partido);

                        }
                        break;
                    }
                    case "4":{
                        for(Partido partido : allGames.values()){
                            System.out.println(String.format("%d. %s %d | %d %s", partido.id_partido, partido.equipo1, partido.equipo1_goles, partido.equipo2_goles, partido.equipo2));
                        }
                        while(true){
                            System.out.println("Ingrese el id del partido a eliminar");
                            System.out.println("Ingrese (0) para volver");
                            String idPartidoString = input.next();
                            if(idPartidoString.equals("0")){
                                break;
                            }
                            if(!isNumeric(idPartidoString)){
                                System.out.println("Ingrese un numero");
                                continue;
                            }
                            int idPartido = Integer.parseInt(idPartidoString);
                            if(!allGames.containsKey(idPartido)){
                                System.out.println("No existe un partido con ese id");
                                continue;
                            }
                            Partido partido = allGames.get(idPartido);
                            borrarPartido(conn, partido);
                            allGames.remove(partido.id_partido);
                            break;
                        }
                        break;
                    }
                    case "5":{
                        for(Partido partido : allGames.values()){
                            System.out.println(String.format("%d. %s %d | %d %s", partido.id_partido, partido.equipo1, partido.equipo1_goles, partido.equipo2_goles, partido.equipo2));
                        }
                        while(true){
                            System.out.println("Ingrese el id del partido para mostrar sus jugadores");
                            System.out.println("Ingrese (0) para volver");
                            String idPartidoString = input.next();
                            if(idPartidoString.equals("0")){
                                break;
                            }
                            if(!isNumeric(idPartidoString)){
                                System.out.println("Ingrese un numero");
                                continue;
                            }
                            int idPartido = Integer.parseInt(idPartidoString);
                            if(!allGames.containsKey(idPartido)){
                                System.out.println("No existe un partido con ese id");
                                continue;
                            }
                            Partido partido = allGames.get(idPartido);
                            mostrarJugadoresPartidos(conn, partido);
                            break;
                        }
                        break;
                    }
                    case "0":{
                        break;
                    }
                    default:{
                        System.out.println("Entrada invalida!");
                    }
                }
            } while (!option.equals("0"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Dotenv dotenv = Dotenv.load();
        Properties props = new Properties();
        props.setProperty("user", dotenv.get("PG_USER"));
        props.setProperty("password", dotenv.get("PG_PASS"));
        String url = dotenv.get("PG_URL");
        String menuPrincipal =
                        "===========================================\n" +
                        "BIENVENIDO AL GESTOR DE PARTIDOS DE FUTBOL!\n" +
                        "===========================================\n" +
                        "Elija una opcion: \n" +
                        "1.Revisar/Editar lista de jugadores\n" +
                        "2.Revisar/Editar lista de equipos\n" +
                        "3.Revisar/Editar lista de partidos\n" +
                        "0.Salir\n";

        try {
            Connection conn = DriverManager.getConnection(url, props);
            String option;
            do{
                System.out.println(menuPrincipal);
                option = input.next();
                switch (option){
                    // Revisar lista de jugadores
                    case "1":{
                        revistarListaJugadores(conn, input);
                        break;
                    }
                    // Revisar lista de equipos
                    case "2":{
                        revistarListaEquipos(conn, input);
                        break;
                    }
                    //Revisar lista de partidos
                   case "3":{
                        revisarListaPartidos(conn, input);
                        break;
                    }
                    // Salir del menu Principal
                    case "0":{
                        break;
                    }
                    // Input no valido
                    default:{
                        System.out.println("Entrada no valido.");
                        break;
                    }
                }

            }while(!option.equals("0"));
            conn.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
