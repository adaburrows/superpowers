/*
 * Copyright (c) 2003, 2007-11 Matteo Frigo
 * Copyright (c) 2003, 2007-11 Massachusetts Institute of Technology
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

/* This file was automatically generated --- DO NOT EDIT */
/* Generated on Sat Apr 28 11:03:52 EDT 2012 */

#include "codelet-rdft.h"

#ifdef HAVE_FMA

/* Generated by: ../../../genfft/gen_r2cb.native -fma -reorder-insns -schedule-for-pipeline -compact -variables 4 -pipeline-latency 4 -sign 1 -n 2 -name r2cb_2 -include r2cb.h */

/*
 * This function contains 2 FP additions, 0 FP multiplications,
 * (or, 2 additions, 0 multiplications, 0 fused multiply/add),
 * 3 stack variables, 0 constants, and 4 memory accesses
 */
#include "r2cb.h"

static void r2cb_2(R *R0, R *R1, R *Cr, R *Ci, stride rs, stride csr, stride csi, INT v, INT ivs, INT ovs)
{
     {
	  INT i;
	  for (i = v; i > 0; i = i - 1, R0 = R0 + ovs, R1 = R1 + ovs, Cr = Cr + ivs, Ci = Ci + ivs, MAKE_VOLATILE_STRIDE(rs), MAKE_VOLATILE_STRIDE(csr), MAKE_VOLATILE_STRIDE(csi)) {
	       E T1, T2;
	       T1 = Cr[0];
	       T2 = Cr[WS(csr, 1)];
	       R0[0] = T1 + T2;
	       R1[0] = T1 - T2;
	  }
     }
}

static const kr2c_desc desc = { 2, "r2cb_2", {2, 0, 0, 0}, &GENUS };

void X(codelet_r2cb_2) (planner *p) {
     X(kr2c_register) (p, r2cb_2, &desc);
}

#else				/* HAVE_FMA */

/* Generated by: ../../../genfft/gen_r2cb.native -compact -variables 4 -pipeline-latency 4 -sign 1 -n 2 -name r2cb_2 -include r2cb.h */

/*
 * This function contains 2 FP additions, 0 FP multiplications,
 * (or, 2 additions, 0 multiplications, 0 fused multiply/add),
 * 3 stack variables, 0 constants, and 4 memory accesses
 */
#include "r2cb.h"

static void r2cb_2(R *R0, R *R1, R *Cr, R *Ci, stride rs, stride csr, stride csi, INT v, INT ivs, INT ovs)
{
     {
	  INT i;
	  for (i = v; i > 0; i = i - 1, R0 = R0 + ovs, R1 = R1 + ovs, Cr = Cr + ivs, Ci = Ci + ivs, MAKE_VOLATILE_STRIDE(rs), MAKE_VOLATILE_STRIDE(csr), MAKE_VOLATILE_STRIDE(csi)) {
	       E T1, T2;
	       T1 = Cr[0];
	       T2 = Cr[WS(csr, 1)];
	       R1[0] = T1 - T2;
	       R0[0] = T1 + T2;
	  }
     }
}

static const kr2c_desc desc = { 2, "r2cb_2", {2, 0, 0, 0}, &GENUS };

void X(codelet_r2cb_2) (planner *p) {
     X(kr2c_register) (p, r2cb_2, &desc);
}

#endif				/* HAVE_FMA */
