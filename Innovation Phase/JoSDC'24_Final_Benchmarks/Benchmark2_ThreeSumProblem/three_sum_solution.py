def three_sum_solution(filename):

    with open(filename, 'r') as file:
        nums = list(map(int, file.read().strip().split(',')))

    sort(nums)

    n = len(nums)
    triplet_count = 0

    for i in range(n - 2):
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        left, right = i + 1, n - 1
        while left < right:
            total = nums[i] + nums[left] + nums[right]
            if total == 0:
                triplet_count += 1
                left += 1
                right -= 1
                while left < right and nums[left] == nums[left - 1]:
                    left += 1
                while left < right and nums[right] == nums[right + 1]:
                    right -= 1
            elif total < 0:
                left += 1
            else:
                right -= 1

    return triplet_count

def sort(arr):
    if len(arr) > 1:
        mid = len(arr) // 2

        left_half = arr[:mid]
        right_half = arr[mid:]

        sort(left_half)
        sort(right_half)

        merge(arr, left_half, right_half)

def merge(arr, left_half, right_half):
    i = j = k = 0
    while i < len(left_half) and j < len(right_half):
        if left_half[i] < right_half[j]:
            arr[k] = left_half[i]
            i += 1
        else:
            arr[k] = right_half[j]
            j += 1
        k += 1
    while i < len(left_half):
        arr[k] = left_half[i]
        i += 1
        k += 1

    while j < len(right_half):
        arr[k] = right_half[j]
        j += 1
        k += 1

csv_file = "numbers.csv"
result = three_sum_solution(csv_file)
print(f"Number of zero-sum triplets: {result}")

