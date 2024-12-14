module myLatch #(parameter size = 8) (d,en,q);

    input [size-1:0] d;
	 input en;
    output reg [size-1:0] q;
	 // Output
    always @(*) begin
        if (en) begin
            q <= d;    // Update output when enable is high
        end
		  else begin
				q <= q;
		  end
        // If enable is low, output retains its previous state
    end
endmodule 