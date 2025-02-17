import heapq
import re
from collections import defaultdict

R_TYPE = ['ADD', 'SUB', 'AND', 'OR', 'XOR', 'NOR', 'SLT', 'SGT', 'SLL', 'SRL']
I_TYPE = ['LW', 'SW', 'ADDI', 'ORI', 'XORI', 'ANDI', 'SLTI']
BRANCH_JUMP = ['BEQ', 'BNE', 'J', 'JR', 'JAL', 'BGEZ', 'BLTZ']


class Instruction:
    def __init__(self, dest=None, src1=None, src2=None, is_branch_jump=False, 
                 is_label=False, is_nop=False):
        self.dest = dest
        self.src1 = src1
        self.src2 = src2
        self.is_branch_jump = is_branch_jump
        self.is_label = is_label
        self.is_nop = is_nop
    
    @classmethod
    def from_str(cls, instruction_str):
        split_inst = [item.strip(' ,').upper() for item in instruction_str.split()]

        label = re.compile(r'[A-Z_]+\:')
        if label.match(instruction_str.upper()) is not None:
            return cls(is_label=True)

        if instruction_str.upper().startswith('NOP'):
            return cls(is_nop=True)

        if split_inst[0] in BRANCH_JUMP:
            return cls(is_branch_jump=True)

        if split_inst[0] in R_TYPE:
            if split_inst[0] == 'SLL' or split_inst[0] == 'SRL':
                return cls(dest=split_inst[1], src1=split_inst[2])
            return cls(dest=split_inst[1], src1=split_inst[2], src2=split_inst[3])

        elif split_inst[0] in I_TYPE:
            if split_inst[0] == 'LW' or split_inst[0] == 'SW':
                pattern = re.compile(r'\((.+)\)')
                match = pattern.search(instruction_str).group(1)
                if split_inst[0] == 'LW':
                    return cls(dest=split_inst[1], src1=match)
                return cls(src1=split_inst[1], src2=match)
            return cls(dest=split_inst[1], src1=split_inst[2])
    
    def __repr__(self):
        return f'{self.dest} = {self.src1} op {self.src2}'


def read_instructions(path_str: str) -> list[str]:
    with open(path_str, 'r') as file_handler:
        return file_handler.read().strip().split('\n')


def build_graphs(instruction_list: list[Instruction], 
                 instruction_strings: list[str]):
    splits = [-1]
    for idx, instruction in enumerate(instruction_list):
        if instruction.is_label or instruction.is_branch_jump:
            splits.append(idx)
    splits.append(len(instruction_list))
    
    ins_ranges = []
    for idx in range(len(splits)-1):
        ins_ranges.append((splits[idx] + 1, splits[idx + 1] - 1))

    scheduled_instructions = []
    for ins_range in ins_ranges:
        instruction_count = ins_range[1] - ins_range[0] + 1
        adj = {}
        for i in range(instruction_count):
            adj[ins_range[0] + i] = []
        for first_line_idx in range(ins_range[0], ins_range[1] + 1):
            first_inst = instruction_list[first_line_idx]
            for second_line_idx in range(first_line_idx + 1, ins_range[1] + 1):
                second_inst = instruction_list[second_line_idx]
                if any((first_inst.dest == second_inst.dest and first_inst.dest != '$0' and first_inst.dest is not None,
                        first_inst.dest == second_inst.src1 and first_inst.dest != '$0' and first_inst.dest is not None,
                        first_inst.dest == second_inst.src2 and first_inst.dest != '$0' and first_inst.dest is not None,
                        second_inst.dest == first_inst.src1 and second_inst.dest != '$0' and second_inst.dest is not None,
                        second_inst.dest == first_inst.src2 and second_inst.dest != '$0' and second_inst.dest is not None)):
                    adj[first_line_idx].append(second_line_idx)
        

        scheduled_instructions.extend([
            instruction_strings[idx] 
            if idx != -1 
            else 'nop' 
            for idx in topo_sort(ins_range[0], instruction_count, adj)
        ])
        if ins_range[1] + 1 < len(instruction_list):
            scheduled_instructions.append(instruction_strings[ins_range[1] + 1])
    scheduled_instructions.append('NOP')
    print('\n'.join(scheduled_instructions))


def topo_sort(start: int, num_of_nodes: int, 
              adj: list[list[int]]) -> list[int]:
    """Performs topological sort on graph and returns sorted instructions"""
    in_degree = defaultdict(int)
    for idx in range(start, start + num_of_nodes):
        for end_node in adj[idx]:
            in_degree[end_node] += 1

    min_heap = []
    for idx in range(start, start + num_of_nodes):
        if in_degree[idx] == 0:
            heapq.heappush(min_heap, idx)

    instruction_order = []
    while min_heap:
        inst1 = heapq.heappop(min_heap)
        inst2 = -1
        if min_heap:
            inst2 = heapq.heappop(min_heap)

        for end_node in adj[inst1]:
            in_degree[end_node] -= 1
            if in_degree[end_node] == 0:
                heapq.heappush(min_heap, end_node)

        if inst2 != -1:
            for end_node in adj[inst2]:
                in_degree[end_node] -= 1
                if in_degree[end_node] == 0:
                    heapq.heappush(min_heap, end_node)
        
        instruction_order.append(inst1)        
        instruction_order.append(inst2)
    
    if instruction_order and instruction_order[-1] == -1:
        instruction_order.pop()

    return instruction_order


def main():
    instruction_strings = read_instructions('instructions.txt')
    instruction_list = [Instruction.from_str(instruction_str) for instruction_str in instruction_strings]
    build_graphs(instruction_list, instruction_strings)


if __name__ == '__main__':
    main()
