package com.example.tptsb;

import java.io.Serializable;
import java.util.*;

public class TSBHashTableDA<K,V> implements Map<K,V>, Cloneable, Serializable
{
    //Estados posibles de las celdas
    public static final int OPEN = 0;
    public static final int CLOSED = 1;
    public static final int TOMBSTONE = 2;

    private int tumbas;
    //La tabla hash en donde iran los objetos a almacenar
    private Object table[];

    //Capacidad inicial que tendra la tabla
    private int initial_capacity;

    //Cantidad de objetos que tiene la tabla
    private int count;

    //Factor de carga para saber si es necesario hacer rehash
    private float load_factor;

    //Cantidad de modificaciones que se le hacen a la tabla
    protected transient int modCount;

    //Tamaño maximo admitido
    private int MAX_SIZE = Integer.MAX_VALUE;

    //atributos para gestionar las vistas

    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K,V>> entrySet = null;
    private transient Collection<V> values = null;

    @Override
    public int size()
    {
        return this.count;
    }

    @Override
    public boolean isEmpty()
    {
        return (this.count == 0);
    }

    @Override
    public boolean containsKey(Object key)
    {
        return (this.get((K)key) != null);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return this.contains(value);
    }

    public boolean contains(Object value)
    {
        if(value == null) return false;

        for(Object entry : this.table)
        {
            Entry <K,V> e = (Entry<K, V>) entry;

            if(value instanceof TSBHashTableDA.Entry){
                if (value.equals(e)){
                    return true;
                }
            }
            else{
                if(value.equals(e.getValue())){
                    return true;
                }
            }


        }

        return false;
    }

    public TSBHashTableDA(){
        this(11,0.5f);
    }

    @Override
    public V get(Object key)
    {
        if(key == null) throw new NullPointerException("get(): parámetro null");
        Map.Entry<K, V> x = this.search_for_entry((K)key,key.hashCode());
        return (x != null)? x.getValue() : null;
    }

    @Override
    public V put(K key, V value)
    {


        if(key == null || value == null) throw new NullPointerException("put(): parámetro null");

        V old = null;

        if(this.load_level() >= this.load_factor){
            this.rehash();
        }
        int ik = this.h(key);
        Map.Entry<K, V> x = this.search_for_entry((K)key, ik);

        if(x != null)
        {
            old = x.getValue();
            x.setValue(value);
        }
        else
        {
            int pos = search_for_OPEN(this.table, this.h(key));
            Map.Entry<K, V> entry = new Entry<>(key, value, CLOSED);
            table[pos] = entry;
            this.count++;
            this.modCount++;
        }
        return old;
    }

    @Override
    public V remove(Object key)
    {
        if(key == null) throw new NullPointerException("remove(): parámetro null");
        int index = search_for_index((K)key,key.hashCode());
        V old = null;
        if(index != -1){
            Entry<K,V> e = (Entry<K, V>) table[index];
            old = e.getValue();

            Entry<K,V> eNuevo = new Entry<K,V>(null,null,TOMBSTONE);
            table[index] = eNuevo;
            this.count--;
            this.modCount++;
            this.tumbas++;
        }
        return old;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        for(Map.Entry<? extends K, ? extends V> e : m.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear()
    {
        this.table = new Object[this.initial_capacity];
        for(int i = 0; i < this.table.length; i++)
        {
            this.table[i] = new Entry<K,V>(null,null);
        }
        this.count = 0;
        this.modCount++;
    }

    @Override
    public Set<K> keySet()
    {
        if(keySet == null)
        {
            keySet = new KeySet();
        }
        return keySet;
    }
    @Override
    public Collection<V> values()
    {
        if(values==null)
            values=new ValueCollection();

        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        if(entrySet == null)
        {
            // entrySet = Collections.synchronizedSet(new EntrySet());
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        TSBHashTableDA<K, V> t = (TSBHashTableDA<K, V>)super.clone();
        t.table = new Object[table.length];
        System.arraycopy(this.table, 0, t.table, 0, this.table.length);

        t.keySet = null;
        t.entrySet = null;
        t.values = null;
        t.modCount = 0;
        return t;

    }
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Map)) { return false; }

        Map<K, V> t = (Map<K, V>) obj;
        if(t.size() != this.size()) { return false; }

        try
        {
            Iterator<Map.Entry<K,V>> i = this.entrySet().iterator();
            while(i.hasNext())
            {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();



                if(t.get(key) == null) { return false; }
                else
                {
                    if(!value.equals(t.get(key))) { return false; }
                }
            }
        }

        catch (ClassCastException | NullPointerException e)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        if(this.isEmpty()) return 0;
        return Arrays.hashCode(this.table);

    }

    @Override
    public String toString()
    {
        StringBuilder cad = new StringBuilder("");
        for(int i = 0; i < this.table.length; i++)
        {
            Entry<K,V> e = (Entry<K, V>) table[i];
            if (e.getState() == CLOSED){
                cad.append("\nElemento ").append(i).append(":\n\t").append(this.table[i].toString());
            }

        }
        return cad.toString();
    }

    public TSBHashTableDA(int initial_capacity, float load_factor)
    {
        if(load_factor <= 0 || load_factor > 0.5) { load_factor = 0.5f; }
        if(initial_capacity <= 0) { initial_capacity = 11; }
        else {
            if (!esPrimo(initial_capacity)) {
                initial_capacity = proxPrimo(initial_capacity);

            }
        }
        this.table = new Object[initial_capacity];
        for (int i = 0; i < table.length; i++) {
            table[i] = new Entry<K, V>(null, null);
        }
        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;

    }

    private int proxPrimo(int n){
        for ( ; !esPrimo(n); n++ ) ;
        return n;
    }
    private Boolean esPrimo(int numero)
    {
        Boolean esPrimoActual = true;
        if(numero<2)
        {
            esPrimoActual = false;
        }
        else
        {
            for(int x=2; x*x<=numero; x++)
            {
                if( numero%x==0 ){esPrimoActual = false;break;}
            }
        }
        return esPrimoActual;
    }

    private float load_level ()
    {
        return (float) (this.count+tumbas) / this.table.length;
    }

    private int search_for_OPEN(Object table[], int ik )
    {
        for (int i = 0; ; i++) {
            ik += (int) Math.pow(i,2);
            ik %= table.length;

            Entry<K,V> e = (Entry<K,V>) table[ik];

            if (e.getState() == OPEN){
                return ik;
            }

        }
    }

    private Map.Entry<K, V> search_for_entry(K key, int ik)
    {
        int pos = search_for_index(key , ik);
        if (pos != -1){
            return (Map.Entry<K,V>) table[pos];
        }
        else{
            return null;
        }
    }

    private int search_for_index(K key, int ik)
    {
        if(ik < 0){
            ik = ik * -1;
        }
        for (int i = 0; ; i++) {
            ik += (int)Math.pow(i,2);
            ik %= table.length;

            Entry<K,V> entry = (Entry<K,V>) table[ik];

            if (entry.getState() == OPEN) {
                return -1;
            }
            if(key.equals(entry.getKey())){
                return ik;
            }
        }
    }

    private void rehash()
    {
        int old_length = this.table.length;
        int new_length = proxPrimo(old_length * 2 + 1);


        if(new_length > this.MAX_SIZE)
        {
            new_length = this.MAX_SIZE;
        }
        Map.Entry<K, V>[] temp = new Map.Entry[new_length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = new Entry<K, V>(null, null);
        }
        this.modCount++;


        for(int i = 0; i < this.table.length; i++)
        {
            Entry<K,V> entry = (Entry<K,V>) table[i];
            if (entry.getState() == CLOSED)
            {
                K key = entry.getKey();
                int ik = this.h(key,temp.length);
                int index = search_for_OPEN(temp,ik);
                temp[index] = entry;
            }
        }
        this.table = temp;
        this.tumbas=0;
    }

    private int h(int k)
    {
        return h(k, this.table.length);
    }
    private int h(K key)
    {
        return h(key.hashCode(), this.table.length);
    }
    private int h(K key, int t)
    {
        return h(key.hashCode(), t);
    }
    private int h(int k, int t)
    {
        if(k < 0) k *= -1;
        return k % t;
    }

    private class Entry<K,V> implements Map.Entry<K,V>{

        private K key;
        private V value;
        private int state;

        public Entry(K key, V value)
        {
            this.key = key;
            this.value = value;
            this.state = OPEN;
        }

        public Entry(K key, V value, int state)
        {
            this.key = key;
            this.value = value;
            this.state = state;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        public int getState(){
            return state;
        }
        @Override
        public V setValue(V value)
        {
            if(value == null){
                throw new IllegalArgumentException("setValue(): parametro null.");

            }
            V old = this.value;
            this.value = value;
            return old;
        }

        public void setState(int state)
        {
            if (state >= 0 && state <= 2)
            {
                this.state = state;
            }
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(this == obj){
                return true;
            }
            if(obj == null){
                return false;
            }
            if(this.getClass() != obj.getClass())
            {
                return false;
            }

            final Entry other = (Entry) obj;
            if(!Objects.equals(this.key, other.key))
            {
                return false;
            }
            if(!Objects.equals(this.value,other.value))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            if(key != null || value != null){
                return "(" + key.toString() + "," + value.toString() + ")";
            }
            return "";
        }
    }


    private class ValueCollection extends AbstractCollection<V>
    {

        @Override
        public Iterator<V> iterator()
        {
            return new ValueCollectionIterator();
        }

        @Override
        public int size()
        {
            return TSBHashTableDA.this.count;
        }

        @Override
        public boolean contains(Object o)
        {
            return TSBHashTableDA.this.containsValue(o);
        }

        @Override
        public void clear()
        {
            TSBHashTableDA.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V>
        {
            private int current_entry;
            private int current_index;
            private boolean next_ok;
            private int expected_modCount;
            private int last_index;

            public ValueCollectionIterator()
            {
                current_entry = 0;
                last_index = 0;
                current_index = -1;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;

            }

            @Override
            public boolean hasNext()
            {

                if (TSBHashTableDA.this.isEmpty())
                {
                    return false;
                }
                if (current_index >= TSBHashTableDA.this.table.length - 1 || current_entry >= TSBHashTableDA.this.count)
                {
                    return false;
                }
                return true;
            }

            @Override
            public V next()
            {
                // control: fail-fast iterator...
                if (TSBHashTableDA.this.modCount != expected_modCount)
                {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext() || isEmpty() == true)
                {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                last_index = current_index;
                current_index++;
                next_ok = true;
                Entry<K,V> e = (Entry<K, V>) TSBHashTableDA.this.table[current_index];
                while(hasNext() && e.getState() != CLOSED ){
                    current_index++;
                    e = (Entry<K, V>) TSBHashTableDA.this.table[current_index];
                }
                current_entry++;
                V val = e.getValue();
                return val;
            }

            @Override
            public void remove()
            {
                if (!next_ok)
                {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                // eliminar el objeto que retornó next() la última vez...
                Entry<K, V> ce = (Entry<K, V>) table[current_index];
                TSBHashTableDA.this.remove(ce.getKey());


                // quedar apuntando al anterior al que se retornó...
                if (last_index != current_index)
                {
                    current_index = last_index;
                }

                next_ok = false;
                expected_modCount++;
            }

        }
    }

        private class KeySet extends AbstractSet<K>{

            @Override
            public Iterator<K> iterator()
            {
                return new KeySetIterator();
            }

            @Override
            public int size()
            {
                return TSBHashTableDA.this.count;
            }
            @Override
            public boolean contains(Object o)
            {
                return TSBHashTableDA.this.containsKey(o);
            }

            @Override
            public boolean remove(Object o)
            {
                return (TSBHashTableDA.this.remove(o) != null);
            }

            @Override
            public void clear()
            {
                TSBHashTableDA.this.clear();
            }

            private class KeySetIterator implements Iterator<K>
            {

                // flag para controlar si remove() está bien invocado...
                private boolean next_ok;

                // el valor que debería tener el modCount de la tabla completa...

                private int current_entry;

                private int expected_modCount;
                private int last_index;
                private int current_index;

                public KeySetIterator(){
                    last_index = 0;
                    current_entry = 0;
                    current_index = -1 ;
                    next_ok = false;
                    expected_modCount = TSBHashTableDA.this.modCount;
                }

                @Override
                public boolean hasNext()
                {


                    if(TSBHashTableDA.this.isEmpty()) { return false; }
                    if(current_index >= TSBHashTableDA.this.table.length - 1 || current_entry >= TSBHashTableDA.this.count) { return false; }
                    return true;
                }

                @Override
                public K next(){
                    if(TSBHashTableDA.this.modCount != expected_modCount){
                        throw new ConcurrentModificationException(("next(): modificación inesperada"));
                    }
                    if(!hasNext()){
                        throw new NoSuchElementException(("next(): no existe el elemento"));
                    }

                    if(!TSBHashTableDA.this.isEmpty() && current_index < TSBHashTableDA.this.table.length - 1)
                    {
                        current_index++;
                    }

                    next_ok = true;
                    Entry<K,V> entry = (Entry<K, V>) TSBHashTableDA.this.table[current_index];

                    while(hasNext() && entry.getState() != CLOSED ){
                        current_index++;
                        entry = (Entry<K, V>) TSBHashTableDA.this.table[current_index];
                    }
                    current_entry++;

                    K key = entry.getKey();
                    return key;
                }
                public void remove() {
                    if (!this.next_ok) {
                        throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                    }
                    else {
                        Entry<K,V> entry = (Entry<K, V>) table[current_index];
                        TSBHashTableDA.this.remove(entry.getKey());
                        if (this.last_index != this.current_index) {
                            this.current_index = this.last_index;
                        }

                        this.next_ok = false;
                        ++this.expected_modCount;
                    }
                }

            }
        }
        private class EntrySet extends AbstractSet<java.util.Map.Entry<K, V>> {

            @Override
            public Iterator<Map.Entry<K, V>> iterator()
            {
                return new EntrySetIterator();
            }

            @Override
            public int size()
            {
                return TSBHashTableDA.this.count;
            }
            @Override
            public boolean contains(Object o)
            {
                if(o == null) { return false; }
                if(!(o instanceof TSBHashTableDA.Entry)) { return false; }

                Map.Entry<K, V> entry = (Map.Entry<K,V>)o;
                if(TSBHashTableDA.this.contains(entry)) { return true; }
                return false;
            }
            @Override
            public boolean remove(Object o)
            {
                if(o == null) { throw new NullPointerException("remove(): parámetro null");}
                if(!(o instanceof Map.Entry)) { return false; }

                Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
                K key = entry.getKey();
                V old = TSBHashTableDA.this.remove(key);

                if(old != null)
                {
                    return true;
                }
                return false;
            }
            @Override
            public void clear()
            {
                TSBHashTableDA.this.clear();
            }
            private class EntrySetIterator implements Iterator<Map.Entry<K, V>>
            {
                private int current_entry;
                private int current_index;
                private int last_index;
                private boolean next_ok;
                private int expected_modCount;

                public EntrySetIterator()
                {
                    current_entry = 0;
                    last_index = 0;
                    current_index = -1 ;
                    next_ok = false;
                    expected_modCount = TSBHashTableDA.this.modCount;

                }

                @Override
                public boolean hasNext()
                {
                    if(TSBHashTableDA.this.isEmpty()) { return false; }
                    if(current_index >= TSBHashTableDA.this.table.length - 1 || current_entry >= TSBHashTableDA.this.count) { return false; }
                    return true;
                }

                @Override
                public Map.Entry<K, V> next()
                {
                    if(TSBHashTableDA.this.modCount != expected_modCount)
                    {
                        throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                    }
                    if(!hasNext())
                    {
                        throw new NoSuchElementException("next(): no existe el elemento pedido...");
                    }
                    if(!TSBHashTableDA.this.isEmpty() && current_index < TSBHashTableDA.this.table.length - 1)
                    {
                        current_index++;
                    }
                    next_ok = true;
                    Entry<K,V> entry = (Entry<K, V>) TSBHashTableDA.this.table[current_index];
                    while(hasNext() && entry.getState() != CLOSED ){
                        current_index++;
                        entry = (Entry<K, V>) TSBHashTableDA.this.table[current_index];
                    }
                    current_entry++;
                    return entry;
                }

                @Override
                public void remove()
                {
                    if (!this.next_ok) {
                        throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                    }
                    else {
                        Entry<K,V> entry = (Entry<K, V>) table[current_index];
                        TSBHashTableDA.this.remove(entry.getKey());
                        if (this.last_index != this.current_index) {
                            this.current_index = this.last_index;
                        }

                        this.next_ok = false;
                        ++this.expected_modCount;
                    }


                }

            }
        }
    }



