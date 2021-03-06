import javafx.geometry.Pos;

import java.util.*;

/**
 * @Classname DoubleArrayAC
 * @Description 双数组AC算法实现
 * @Date 2020/11/5 12:03
 * @Created by shuaif
 */
public class DoubleArrayAC {

    //Next表，下标是位置偏移量，输出状态值
    private Map<Integer, Integer> next = new HashMap<>();
    //Base表，下标是状态值，输出是base值，要求:当前状态的Base值+ASCII输入=下一个状态的偏移
    private Map<Integer, Integer> base = new HashMap<>();
    //Check表，当前状态的父状态信息（唯一）
    private Map<Integer, Integer> check = new HashMap<>();

    private int curPos = 0;

    private State originState;
    private List<State> allStates = new ArrayList<>(); //下标即id


    public DoubleArrayAC(String[] pattern) {
        originState = new State(0);
        allStates.add(originState);
        constructAutomata(pattern);
    }


    /**
     * 构建自动机
     * @param pattern 模式串集合
     */
    private void constructAutomata(String[] pattern){
        for (String s : pattern) {
            enter(s);
        }
        originState.setFailure(originState);
        failure();

        // 建表
        base.put(0,-98);
        next.put(0,0);
        check.put(0,0);
        for (int i = 0; i < allStates.size(); i++) {
            State state = allStates.get(i);
            for (State child : state.getChildren()) { //check表构建
                if (!check.containsKey(child.getState())) {
                    check.put(child.getState(),i);
                }
            }
            List<Integer> list = new ArrayList<>();
            List<Integer> states = new ArrayList<>();
            List<Integer> bytes = new ArrayList<>();
            for (Byte b : state.getTrans().keySet()) {
                int offest = base.get(state.getState()) + b + getOffset(state.getState());
                list.add(offest);
                states.add(state.getTrans().get(b).getState());
                bytes.add(Integer.valueOf(b));
            }
            for (int i1 = 0; i1 < list.size(); i1++) {
                next.put(list.get(i1),states.get(i1));
                base.put(states.get(i1), (list.get(i1) - curPos + base.get(state.getState())));
            }
        }
    }

    /**
     * 添加新的状态节点到自动机上面
     * @param s 模式串
     */
    private void enter(String s) {
        byte[] key = s.getBytes();
        int i = 0;
        State state = originState;
        while (true) {
            assert state != null;
            if (!state.getTrans().containsKey(key[i])) break; //当前状态没有对应的转移状态
            state = _goto(state,key[i]);
            i++;
        }
        for (int p = i; p < key.length; p++){
            System.out.println(new String(new byte[] {key[p]}) + "  "+ state.getState());
            State newState = new State(allStates.size());
            state.addGoto(key[p], newState);
            state = newState;
            allStates.add(newState);
        }
        state.addOutput(new String(key));
    }


    /**
     * goto 函数：状态转移
     * @param state
     * @param input
     * @return
     */
    private State _goto(State state, byte input) {
        if (state.getTrans().containsKey(input)) {
            return state.getTrans().get(input);
        }
        if (state.getState() == 0) {
            return originState;
        }
        return null;
    }

    private State _goto_2(State state, byte input){
        assert getOffset(state.getState()) == -1;
//        System.out.println(getOffset(state.getState()));
//        System.out.println(getOffset(state.getState()) + base.get(state.getState()) + input);
//        System.out.println("input : " + input);
        int offest = getOffset(state.getState()) + base.get(state.getState()) + input;
        if (!next.containsKey(offest)) {
            offest = 0;
        }
        int t = next.get(offest);
        if (check.get(t) == state.getState()){
//            System.out.println("curState :" + t);
            return allStates.get(t);
        } else {
            System.out.println("Fail");
            return null;
        }
    }

    private Integer getOffset(int state) {
        for (Integer integer : next.keySet()) {
            if (state == next.get(integer)) {
                return integer;
            }
        }
        return -1;
    }

    /**
     * 失效函数
     * @return
     */
    private void failure() {
        Queue<State> queue = new LinkedList<>();
        for (State child : originState.getChildren()) { //深度为1的节点failure值为0
            queue.offer(child);
            child.setFailure(originState);
        }
        while (!queue.isEmpty()) {
            State r = queue.poll();
            for (Byte key : r.getTrans().keySet()) {
                State s = r.getTrans().get(key);
                if (!queue.contains(s)){
                    queue.offer(s);
                }
                State state = r.getFailure();
                while (_goto(state,key) == null) {
                    state = state.getFailure();
                }
                s.setFailure(_goto(state,key));
                for (String output : s.getFailure().getOutput()) {
                    s.addOutput(output);
                }
            }
        }
    }


    public void run(String input){
        byte[] target = input.getBytes();
        State state = originState;
        for (byte b : target) {
            while (_goto(state,b) == null) {
                state = state.getFailure();
            }
            state = _goto(state,b);
            assert state != null;
            if (state.isTerminalState()) {
                System.out.println("State : " + state.getState());
                System.out.print("Output : { ");
                for (String s : state.getOutput()) {
                    System.out.print(s + ",");
                }
                System.out.println("}");
            }
        }
    }

    public void run_2(String input){
        byte[] target = input.getBytes();
        State state = originState;
        for (byte b : target) {
            while (_goto_2(state,b) == null) {
                state = state.getFailure();
            }
            state = _goto_2(state,b);
//            System.out.println("run_2 state: " + state.getState());
            assert state != null;
            if (state.isTerminalState()) {
                System.out.println("State : " + state.getState());
                System.out.print("Output : { ");
                for (String s : state.getOutput()) {
                    System.out.print(s + ",");
                }
                System.out.println("}");
            }
        }
    }

    public void showAutomata() {
        for (State state : allStates) {
            System.out.println("ID :"+state.getState());
            System.out.print("Trans : ");
            for (Byte key : state.getTrans().keySet()) {
                System.out.print("[ " + new String(new byte[] {key}) + "," + state.getTrans().get(key).getState() +  " ]");
            }
            System.out.println();
            System.out.println("Failure : " + state.getFailure().getState());
            if (state.isTerminalState()) {
                System.out.print("Output : [");
                for (String s : state.getOutput()) {
                    System.out.print(s + " ");
                }
                System.out.println("]");
            }
        }
    }

    public void showAutomata_2(){
        System.out.println("Next : ");
        System.out.print("[");
        for (Integer integer : next.keySet()) {
            System.out.print(integer + " " + next.get(integer) + "; ");
        }
        System.out.println("]");
        System.out.println("Base : ");
        System.out.print("[");
        for (Integer integer : base.keySet()) {
            System.out.print(integer + " " + base.get(integer) + "; ");
        }
        System.out.println("]");
        System.out.println("Check : ");
        System.out.print("[");
        for (Integer integer : check.keySet()) {
            System.out.print(integer + " " + check.get(integer) + "; ");
        }
        System.out.println("]");
    }

    public static void main(String[] args) {
        String[] pattern = {"she","he","his"};
        DoubleArrayAC daac = new DoubleArrayAC(pattern);
        daac.showAutomata_2();
        daac.run_2("he his shehe");
    }

}
class State{
    private final int state;
    private Map<Byte,State> trans = new HashMap<>();
    private Set<State> children = new HashSet<>();
    private Set<String> output = new HashSet<>();
    private State failure = null;

    public State(int state) {
        this.state = state;
    }

    public void addGoto(byte input, State target){
        if (!trans.containsKey(input)) {
            this.trans.put(input,target);
            children.add(target);
        }
    }

    public int getState() {
        return state;
    }

    public Map<Byte, State> getTrans() {
        return trans;
    }

    public State getFailure() {
        return failure;
    }

    public Set<String> getOutput() {
        return output;
    }

    public Set<State> getChildren() {
        return children;
    }

    /**
     * 向指定状态添加输出表
     * @param key 命中结果
     */
    public void addOutput(String key) {
        output.add(key);
    }

    public boolean isTerminalState() {
        return !output.isEmpty();
    }

    public void setFailure(State state){
        this.failure = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state1 = (State) o;
        return getState() == state1.getState();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getState());
    }
}
