from copy import deepcopy

MEM_SIZE = 256


class MEM_WB:
    def __init__(self):
        self.reg_write = 0
        self.mem_to_reg = 0

        self.instruction = '00000000000000000000000000000000'
        self.pc_plus_1 = 0
        self.alu_result = 0
        self.memory_read_data = 0
        self.write_register = 0


class EX_MEM:
    def __init__(self):
        self.reg_write = 0
        self.mem_to_reg = 0
        self.mem_write = 0
        self.mem_read = 0

        self.instruction = '00000000000000000000000000000000'
        self.pc_plus_1 = 0
        self.alu_result = 0
        self.forward_B_mux_out = 0
        self.write_register = 0


class ID_EX:
    def __init__(self):
        self.reg_write = 0
        self.mem_to_reg = 0
        self.mem_write = 0
        self.mem_read = 0
        self.alu_op = 0
        self.reg_dst = 0
        self.alu_src = 0
        self.branch = 0
        self.I_26 = 0
        self.prediction = 0

        self.instruction = '00000000000000000000000000000000'
        self.pc = 0
        self.branch_adder = 0
        self.pc_plus_1 = 0
        self.read_data1 = 0
        self.read_data2 = 0
        self.ext_imm = 0
        self.rs = 0
        self.rt = 0
        self.rd = 0
        self.shamt = 0

    def flush(self):
        self.__init__()


class IF_ID:
    def __init__(self):
        self.pc = 0
        self.branch_adder = 0
        self.pc_plus_1 = 0
        self.prediction = 0
        self.instruction = '00000000000000000000000000000000'

    def flush(self):
        self.__init__()


class State:
    def __init__(self):
        self.if_id = IF_ID()
        self.id_ex = ID_EX()
        self.ex_mem = EX_MEM()
        self.mem_wb = MEM_WB()

    def print(self):
        pass


class RF:
    def __init__(self):
        self.registers = []
        for i in range(32):
            self.registers.append(0)

    def read_rf(self, reg_num):
        return int(self.registers[reg_num])

    def write_rf(self, reg_num, write_data):
        if reg_num != 0:
            self.registers[reg_num] = write_data

    def out_rf(self):
        with open('rf_result.txt', 'w') as f:
            for i in range(32):
                f.write(str(self.registers[i]) + '\n')

    def print_rf(self):
        print(f'RF: {" ".join(map(str, self.registers))}')


class DataMem:
    def __init__(self):
        # with open('data_mem.txt', 'w') as f:
        #     for i in range(MEM_SIZE):
        #         f.write(str(0) + '\n')
        pass

    @staticmethod
    def write_dm(address, data):
        with open('data_mem.txt', 'r') as f:
            read_data = f.read().split('\n')
        read_data[address] = str(data)
        with open('data_mem.txt', 'w') as f:
            for idx, line in enumerate(read_data):
                if idx < len(read_data)-1:
                    f.write(f'{line}\n')
                else:
                    f.write(line)

    @staticmethod
    def read_dm(address):
        with open('data_mem.txt', 'r') as f:
            try:
                read_data = f.readlines()
                return int(read_data[address])
            except ValueError:
                print(f'{address = }')


class InsMem:
    def __init__(self):
        with open('ins_mem.txt', 'r') as f:
            read_data = f.read().split('\n')
        self.instructions = read_data

    def get_instruction(self, address):
        return self.instructions[address]

    def get_offsets(self):
        offsets = {}
        for pc, instruction in enumerate(self.instructions):
            op_code = instruction[:6]
            op_code = hex(int(op_code, 2))
            if op_code == '0x4' or op_code == '0x5':
                offsets[str(pc)] = instruction[-8:]
        return offsets


class PC:
    def __init__(self):
        self.cur_pc = 0

    def update_pc(self, new_pc, EN):
        if EN:
            self.cur_pc = new_pc


class HDU:
    def __init__(self):
        pass

    @staticmethod
    def stall(mem_read_E, write_register_E, rs_D, rt_D):
        if write_register_E and mem_read_E:
            if write_register_E == rs_D or write_register_E == rt_D:
                return 1
        return 0

    @staticmethod
    def flush(branch_taken, prediction_E, branch_E):
        return (branch_taken ^ prediction_E) & branch_E


class BPU:
    def __init__(self):
        self.corrected_pc = 0
        self.BHT = [1 for _ in range(256)]

    def predict(self, pc):
        match self.BHT[pc]:
            case 0:
                return 0
            case 1:
                return 0
            case 2:
                return 1
            case 3:
                return 1

    def update(self, pc, taken_branch):
        match self.BHT[pc]:
            case 0:
                self.BHT[pc] = 1 if taken_branch else 0
            case 1:
                self.BHT[pc] = 2 if taken_branch else 0
            case 2:
                self.BHT[pc] = 3 if taken_branch else 1
            case 3:
                self.BHT[pc] = 3 if taken_branch else 2

    def set_corrected_pc(self, correct_pc):
        self.corrected_pc = correct_pc


def mux(sel, in1, in2, in3=None):
    if sel == 0:
        return in1
    elif sel == 1:
        return in2
    elif sel == 2:
        return in3


def alu(operand1, operand2, alu_shamt, op_sel):
    result = 0
    _overflow = 0

    match op_sel:
        case 0:  # add
            result = operand1 + operand2
        case 1:  # sub
            result = operand1 - operand2
        case 2:  # and
            result = operand1 & operand2
        case 3:  # or
            result = operand1 | operand2
        case 4:  # slt
            result = 1 if operand1 < operand2 else 0
        case 5:  # sgt
            result = 1 if operand1 > operand2 else 0
        case 6:  # nor
            result = ~(operand1 | operand2)
        case 7:  # xor
            result = operand1 ^ operand2
        case 8:  # sll
            result = (operand2 << alu_shamt) & (2 ** 32 - 1)
        case 9:  # srl
            result = operand2 >> alu_shamt
    zero = int(result == 0)

    return result, _overflow, zero


def cu(cu_op_code, cu_funct, cu_stall):
    control_unit = {'reg_dst': 0,
                    'branch': 0,
                    'mem_read': 0,
                    'mem_to_reg': 0,
                    'alu_op': 0,
                    'mem_write': 0,
                    'reg_write': 0,
                    'alu_src': 0,
                    'jump': 0,
                    'pc_src': 0}
    if cu_stall:
        return control_unit
    cu_op_code = hex(int(cu_op_code, 2))
    cu_funct = hex(int(cu_funct, 2))
    if cu_op_code == '0x0':  # R-TYPE
        control_unit.update({'reg_dst': 1,
                             'branch': 0,
                             'mem_read': 0,
                             'mem_to_reg': 0,
                             'alu_op': 0,
                             'mem_write': 0,
                             'reg_write': 1,
                             'alu_src': 0,
                             'jump': 0,
                             'pc_src': 0})

        if cu_funct == '0x20':  # add
            control_unit.update({'alu_op': 0})
        elif cu_funct == '0x22':  # sub
            control_unit.update({'alu_op': 1})
        elif cu_funct == '0x24':  # and
            control_unit.update({'alu_op': 2})
        elif cu_funct == '0x25':  # or
            control_unit.update({'alu_op': 3})
        elif cu_funct == '0x2a':  # slt
            control_unit.update({'alu_op': 4})
        elif cu_funct == '0x14':  # sgt
            control_unit.update({'alu_op': 5})
        elif cu_funct == '0x27':  # nor
            control_unit.update({'alu_op': 6})
        elif cu_funct == '0x15':  # xor
            control_unit.update({'alu_op': 7})
        elif cu_funct == '0x0':  # sll
            control_unit.update({'alu_op': 8})
        elif cu_funct == '0x2':  # srl
            control_unit.update({'alu_op': 9})
        elif cu_funct == '0x8':  # jr
            control_unit.update({'pc_src': 1})

    elif cu_op_code == '0x8':  # addi
        control_unit.update({'reg_write': 1,
                             'alu_src': 1})

    elif cu_op_code == '0x23':  # lw
        control_unit.update({'mem_read': 1,
                             'mem_to_reg': 1,
                             'reg_write': 1,
                             'alu_src': 1})

    elif cu_op_code == '0x2b':  # sw
        control_unit.update({'mem_write': 1,
                             'alu_src': 1})

    elif cu_op_code == '0x4' or cu_op_code == '0x5':  # beq or bne
        control_unit.update({'branch': 1,
                             'alu_op': 1})

    elif cu_op_code == '0x3':  # jal
        control_unit.update({'reg_dst': 2,
                             'mem_to_reg': 2,
                             'reg_write': 1,
                             'jump': 1,
                             'pc_src': 1})

    elif cu_op_code == '0xd':  # ori
        control_unit.update({'alu_op': 3,
                             'reg_write': 1,
                             'alu_src': 1})

    elif cu_op_code == '0x16':  # xori
        control_unit.update({'alu_op': 7,
                             'reg_write': 1,
                             'alu_src': 1})

    elif cu_op_code == '0xc':  # andi
        control_unit.update({'alu_op': 2,
                             'reg_write': 1,
                             'alu_src': 1})

    elif cu_op_code == '0xa':  # slti
        control_unit.update({'alu_op': 4,
                             'reg_write': 1,
                             'alu_src': 1})

    elif cu_op_code == '0x2':  # j
        control_unit.update({'jump': 1,
                             'pc_src': 1})

    return control_unit


def forward(rs_E, rt_E, write_register_M, write_register_W, reg_write_M, reg_write_W):
    forwardA = 0
    forwardB = 0

    if reg_write_M and write_register_M == rs_E and write_register_M != 0:
        forwardA = 1
    elif reg_write_W and write_register_W == rs_E and write_register_W != 0:
        forwardA = 2

    if reg_write_M and write_register_M == rt_E and write_register_M != 0:
        forwardB = 1
    elif reg_write_W and write_register_W == rt_E and write_register_W != 0:
        forwardB = 2
    return forwardA, forwardB


def main():
    rf = RF()
    state = State()
    data_mem = DataMem()
    ins_mem = InsMem()
    pc = PC()
    branch_predictor = BPU()
    hazard_detection_unit = HDU()

    cycle = 0
    enable = 1

    while True:
        new_state = State()
        # --------------------WB STAGE------------------ #
        WB_data = 0
        if state.mem_wb.reg_write == 1:
            WB_data = mux(sel=state.mem_wb.mem_to_reg,
                          in1=state.mem_wb.alu_result,
                          in2=state.mem_wb.memory_read_data,
                          in3=state.mem_wb.pc_plus_1)

            rf.write_rf(reg_num=state.mem_wb.write_register, write_data=WB_data)

        # --------------------MEM STAGE------------------ #
        if state.ex_mem.mem_write:
            data_mem.write_dm(address=state.ex_mem.alu_result, data=state.ex_mem.forward_B_mux_out)
        if state.ex_mem.mem_read:
            new_state.mem_wb.memory_read_data = data_mem.read_dm(state.ex_mem.alu_result)
        new_state.mem_wb.reg_write = state.ex_mem.reg_write
        new_state.mem_wb.mem_to_reg = state.ex_mem.mem_to_reg
        new_state.mem_wb.pc_plus_1 = state.ex_mem.pc_plus_1
        new_state.mem_wb.alu_result = state.ex_mem.alu_result
        new_state.mem_wb.write_register = state.ex_mem.write_register
        new_state.mem_wb.instruction = state.ex_mem.instruction

        # --------------------FORWARDING------------------ #
        forward_A_E, forward_B_E = forward(rs_E=state.id_ex.rs,
                                           rt_E=state.id_ex.rt,
                                           write_register_M=state.ex_mem.write_register,
                                           write_register_W=state.mem_wb.write_register,
                                           reg_write_M=state.ex_mem.reg_write,
                                           reg_write_W=state.mem_wb.reg_write)

        # --------------------EX STAGE------------------ #
        forward_mux_A = mux(sel=forward_A_E,
                            in1=state.id_ex.read_data1,
                            in2=state.ex_mem.alu_result,
                            in3=WB_data)

        forward_mux_B = mux(sel=forward_B_E,
                            in1=state.id_ex.read_data2,
                            in2=state.ex_mem.alu_result,
                            in3=WB_data)

        alu_mux = mux(sel=state.id_ex.alu_src,
                      in1=forward_mux_B,
                      in2=state.id_ex.ext_imm)

        alu_result, overflow, zero = alu(operand1=forward_mux_A,
                                         operand2=alu_mux,
                                         alu_shamt=state.id_ex.shamt,
                                         op_sel=state.id_ex.alu_op)

        reg_dst_mux = mux(sel=state.id_ex.reg_dst,
                          in1=state.id_ex.rt,
                          in2=state.id_ex.rd,
                          in3=31)

        branch_taken = state.id_ex.branch & ~(state.id_ex.I_26 ^ ~zero)

        if state.id_ex.branch:
            branch_predictor.update(pc=state.id_ex.pc, taken_branch=branch_taken)

        flush = hazard_detection_unit.flush(branch_taken=branch_taken,
                                            prediction_E=state.id_ex.prediction,
                                            branch_E=state.id_ex.branch)

        new_state.ex_mem.alu_result = alu_result
        new_state.ex_mem.reg_write = state.id_ex.reg_write
        new_state.ex_mem.mem_to_reg = state.id_ex.mem_to_reg
        new_state.ex_mem.mem_write = state.id_ex.mem_write
        new_state.ex_mem.mem_read = state.id_ex.mem_read
        new_state.ex_mem.pc_plus_1 = state.id_ex.pc_plus_1
        new_state.ex_mem.forward_B_mux_out = forward_mux_B
        new_state.ex_mem.write_register = reg_dst_mux
        new_state.ex_mem.instruction = state.id_ex.instruction

        # --------------------ID STAGE------------------ #
        op_code = state.if_id.instruction[:6]
        funct = state.if_id.instruction[-6:]
        rs = int(state.if_id.instruction[6:11], 2)
        rt = int(state.if_id.instruction[11:16], 2)
        rd = int(state.if_id.instruction[16:21], 2)
        shamt = int(state.if_id.instruction[21:26], 2)
        imm = int(state.if_id.instruction[-16:], 2)
        if imm >> 15 == 1:
            imm = imm - 2 ** 16
        I_26 = int(op_code[-1])

        stall = hazard_detection_unit.stall(mem_read_E=state.id_ex.mem_read,
                                            write_register_E=reg_dst_mux,
                                            rs_D=rs,
                                            rt_D=rt)

        if not stall:
            if branch_taken and not state.id_ex.prediction:
                branch_predictor.set_corrected_pc(state.id_ex.branch_adder)
            elif not branch_taken and state.id_ex.prediction:
                branch_predictor.set_corrected_pc((state.id_ex.pc + 1) & 255)

        control_signals = cu(cu_op_code=op_code, cu_funct=funct, cu_stall=stall)

        read_data1 = rf.read_rf(rs)
        read_data2 = rf.read_rf(rt)

        branch = control_signals.get('branch')

        new_state.id_ex.pc = state.if_id.pc
        new_state.id_ex.branch_adder = state.if_id.branch_adder
        new_state.id_ex.I_26 = I_26
        new_state.id_ex.branch = branch
        new_state.id_ex.reg_write = control_signals.get('reg_write')
        new_state.id_ex.mem_to_reg = control_signals.get('mem_to_reg')
        new_state.id_ex.mem_write = control_signals.get('mem_write')
        new_state.id_ex.mem_read = control_signals.get('mem_read')
        new_state.id_ex.alu_op = control_signals.get('alu_op')
        new_state.id_ex.reg_dst = control_signals.get('reg_dst')
        new_state.id_ex.alu_src = control_signals.get('alu_src')
        new_state.id_ex.prediction = state.if_id.prediction
        new_state.id_ex.pc_plus_1 = state.if_id.pc_plus_1
        new_state.id_ex.read_data1 = read_data1
        new_state.id_ex.read_data2 = read_data2
        new_state.id_ex.ext_imm = imm
        new_state.id_ex.rs = rs
        new_state.id_ex.rt = rt
        new_state.id_ex.rd = rd
        new_state.id_ex.shamt = shamt
        new_state.id_ex.instruction = state.if_id.instruction

        # --------------------IF STAGE------------------ #
        instruction = ins_mem.get_instruction(address=pc.cur_pc)
        prediction = branch_predictor.predict(pc=pc.cur_pc)
        pc_plus_1 = (pc.cur_pc + 1) & 255
        branch_adder = (pc_plus_1 + int(instruction[-8:], 2)) & 255

        jump_mux = mux(sel=control_signals.get('jump'), in1=read_data1 & 255, in2=int(state.if_id.instruction[-8:], 2))
        branch_mux = mux(sel=prediction, in1=pc_plus_1, in2=branch_adder)
        next_pc = mux(sel=control_signals.get('pc_src'), in1=branch_mux, in2=jump_mux)
        cpc_mux = mux(sel=flush, in1=next_pc, in2=branch_predictor.corrected_pc)

        print(f'cycle: {cycle}')
        print(f'pc: {pc.cur_pc}')
        
        enable_pc_IF_ID = (not stall) and enable and cycle
        if enable_pc_IF_ID:
            new_state.if_id.prediction = prediction
            new_state.if_id.pc_plus_1 = pc_plus_1
            new_state.if_id.pc = pc.cur_pc
            new_state.if_id.branch_adder = branch_adder
            new_state.if_id.instruction = instruction
        else:
            new_state.if_id = deepcopy(state.if_id)
        pc.update_pc(cpc_mux, enable_pc_IF_ID)

        if stall:
            new_state.id_ex.flush()

        if flush:
            new_state.if_id.flush()
            new_state.id_ex.flush()

        if control_signals.get('pc_src'):
            new_state.if_id.flush()


        
        print(f'IF: instruction: {instruction}')
        print(f'ID: instruction: {state.if_id.instruction}')
        print(f'EX: instruction: {state.id_ex.instruction}')
        print(f'MEM: instruction: {state.ex_mem.instruction}')
        print(f'WB: instruction: {state.mem_wb.instruction}')
        print(f'\nID: {control_signals}')

        cycle += 1
        state = deepcopy(new_state)
        
        print()

        if pc.cur_pc == 255:
            break

    rf.out_rf()


if __name__ == '__main__':
    main()

# TODO: write overflow in ALU
